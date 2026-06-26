#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import shutil
from collections import defaultdict
from pathlib import Path
from typing import Any, Iterable


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_TARNISH = Path("/Users/tylercovalt/Desktop/tarnish-main/game-server")
SPAWN_ROOT = ROOT / "src/main/kotlin/net/dodian/uber/game/npc"
OUTPUT_ROOT = ROOT / "data/def"
SPAWN_IMPORT_SOURCE = OUTPUT_ROOT / "npc/spawn-import-source.json"

FACING_TO_NAME = {
    -1: "NONE",
    0: "NORTH",
    1: "NORTH_EAST",
    2: "EAST",
    3: "SOUTH_EAST",
    4: "SOUTH",
    5: "SOUTH_WEST",
    6: "WEST",
    7: "NORTH_WEST",
}
FACING_CONSTANTS = {
    "NORTH": 0,
    "north": 0,
    "EAST": 2,
    "east": 2,
    "SOUTH": 4,
    "south": 4,
    "WEST": 6,
    "west": 6,
}

SPAWN_RE = re.compile(
    r"NpcSpawnDef\(\s*npcId\s*=\s*(?P<npc>\d+)\s*,\s*"
    r"x\s*=\s*(?P<x>\d+)\s*,\s*y\s*=\s*(?P<y>\d+)\s*,\s*"
    r"z\s*=\s*(?P<plane>\d+)\s*,\s*face\s*=\s*(?P<facing>-?\d+|[A-Za-z_]+)"
    r"(?P<tail>[^)]*)\)"
)
SPAWN_ENTRIES_RE = re.compile(
    r"spawnEntries\(\s*npcId\s*=\s*(?P<npc>\d+)\s*,(?P<points>.*?)\n\s*\)", re.DOTALL
)
POINT_RE = re.compile(
    r"point\(\s*(?P<x>\d+)\s*,\s*(?P<y>\d+)"
    r"(?:\s*,\s*(?P<plane>\d+))?(?:\s*,\s*(?P<facing>-?\d+|[A-Za-z_]+))?\s*\)"
)


def dump_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n")


def snake_case(name: str) -> str:
    first = re.sub(r"(.)([A-Z][a-z]+)", r"\1_\2", name)
    return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", first).lower()


def read_old_to_new(path: Path) -> dict[int, int]:
    result: dict[int, int] = {}
    for raw in path.read_text().splitlines():
        if "=" not in raw:
            continue
        old, new = raw.split("=", 1)
        result[int(old.strip())] = int(new.strip())
    return result


def parse_scalar(raw: str) -> Any:
    value = raw.strip()
    if value.lower() == "true":
        return True
    if value.lower() == "false":
        return False
    if re.fullmatch(r"-?\d+", value):
        return int(value)
    return value


def parse_tail(tail: str) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in re.findall(r",\s*([A-Za-z][A-Za-z0-9]*)\s*=\s*([^,]+)", tail):
        result[key] = parse_scalar(value)
    return result


def facing_value(raw: str | int) -> int:
    if isinstance(raw, int):
        return raw
    if re.fullmatch(r"-?\d+", raw):
        return int(raw)
    if raw not in FACING_CONSTANTS:
        raise ValueError(f"Unsupported facing constant: {raw}")
    return FACING_CONSTANTS[raw]


def parsed_spawn(npc: int, x: int, y: int, plane: int, facing: int, extra: dict[str, Any]) -> dict[str, Any]:
    # Correct the two malformed committed banker rows using the canonical legacy export.
    if npc == 39444 and (x, y, plane) == (2727, 3378, 0):
        npc = 394
    if npc == 394 and (x, y, plane) == (2615, 394, 0):
        y = 3094

    row: dict[str, Any] = {
        "legacyNpcId": npc,
        "x": x,
        "y": y,
        "plane": plane,
        "facing": FACING_TO_NAME.get(facing, str(facing)),
    }
    optional_map = {
        "live": "enabled",
        "walkRadius": "walkRadius",
        "attackRange": "attackRange",
        "alwaysActive": "alwaysActive",
        "respawnTicks": "respawnTicks",
        "attack": "attack",
        "defence": "defence",
        "strength": "strength",
        "hitpoints": "hitpoints",
        "ranged": "ranged",
        "magic": "magic",
    }
    for source, target in optional_map.items():
        if source in extra:
            row[target] = extra[source]
    return row


def parse_spawn_file(path: Path) -> list[dict[str, Any]]:
    text = path.read_text()
    rows: list[dict[str, Any]] = []
    for match in SPAWN_RE.finditer(text):
        rows.append(
            parsed_spawn(
                npc=int(match.group("npc")),
                x=int(match.group("x")),
                y=int(match.group("y")),
                plane=int(match.group("plane")),
                facing=facing_value(match.group("facing")),
                extra=parse_tail(match.group("tail")),
            )
        )

    for entry_match in SPAWN_ENTRIES_RE.finditer(text):
        npc = int(entry_match.group("npc"))
        for point in POINT_RE.finditer(entry_match.group("points")):
            rows.append(
                parsed_spawn(
                    npc=npc,
                    x=int(point.group("x")),
                    y=int(point.group("y")),
                    plane=int(point.group("plane") or 0),
                    facing=facing_value(point.group("facing") or 0),
                    extra={},
                )
            )
    return rows


def import_spawns(old_to_new: dict[int, int], output: Path) -> dict[str, Any]:
    output.mkdir(parents=True, exist_ok=True)
    for old in output.glob("*.json"):
        old.unlink()

    source_ids: set[int] = set()
    final_ids: set[int] = set()
    mapped_rows = 0
    total_rows = 0
    all_keys: dict[tuple[int, int, int, int], str] = {}
    duplicate_rows: list[dict[str, Any]] = []
    family_reports: list[dict[str, Any]] = []

    if SPAWN_IMPORT_SOURCE.is_file():
        source_document = json.loads(SPAWN_IMPORT_SOURCE.read_text())
        if source_document.get("schemaVersion") != 1:
            raise ValueError(f"Unsupported spawn import source schema in {SPAWN_IMPORT_SOURCE}")
        sources = [
            (entry["family"], entry["source"], entry["rows"])
            for entry in source_document["families"]
        ]
    else:
        sources = []
        for source in sorted(SPAWN_ROOT.rglob("*.kt")):
            if source.name in {"NpcSpawnDef.kt", "NpcPluginDsl.kt", "NpcSpawnGroups.kt"}:
                continue
            rows = parse_spawn_file(source)
            if rows:
                sources.append((snake_case(source.stem), str(source.relative_to(ROOT)), rows))

    for family, source_name, source_rows in sources:
        rows = [dict(row) for row in source_rows]
        if not rows:
            continue
        groups: dict[int, list[dict[str, Any]]] = defaultdict(list)
        conversions: set[tuple[int, int]] = set()
        for row in rows:
            legacy_id = row.pop("legacyNpcId")
            npc_id = old_to_new.get(legacy_id, legacy_id)
            if npc_id != legacy_id:
                mapped_rows += 1
                conversions.add((legacy_id, npc_id))
            source_ids.add(legacy_id)
            final_ids.add(npc_id)
            total_rows += 1
            key = (npc_id, row["x"], row["y"], row["plane"])
            existing = all_keys.get(key)
            if existing is not None:
                duplicate_rows.append({"key": list(key), "firstFamily": existing, "duplicateFamily": family})
                continue
            else:
                all_keys[key] = family
            groups[npc_id].append(row)

        document = {
            "schemaVersion": 1,
            "family": family,
            "groups": [
                {"npcId": npc_id, "spawns": group_rows}
                for npc_id, group_rows in sorted(groups.items())
            ],
        }
        dump_json(output / f"{family}.json", document)
        family_reports.append(
            {
                "family": family,
                "source": source_name,
                "spawnCount": len(rows),
                "conversions": [
                    {"old": old, "new": new} for old, new in sorted(conversions)
                ],
            }
        )

    collisions = []
    for old, new in sorted(old_to_new.items()):
        if old in source_ids and new in source_ids and old != new:
            collisions.append({"old": old, "new": new, "reason": "target ID also existed before migration"})

    return {
        "schemaVersion": 1,
        "strategy": "single-pass-oldtonew-only",
        "totalSpawnRows": total_rows,
        "mappedSpawnRows": mapped_rows,
        "sourceNpcIds": len(source_ids),
        "finalNpcIds": len(final_ids),
        "collisions": collisions,
        "duplicateSpawnRows": duplicate_rows,
        "families": family_reports,
    }


ITEM_FIELDS = (
    "id", "name", "tradeable", "stackable", "noted", "noteable",
    "linked_id_item", "linked_id_noted", "equipable", "equipable_by_player",
    "equipable_weapon", "cost", "lowalch", "highalch", "weight", "examine",
    "equipment", "weapon",
)
MONSTER_FIELDS = (
    "id", "name", "combat_level", "size", "hitpoints", "attack_speed",
    "aggressive", "poisonous", "venomous", "immune_poison", "immune_venom",
    "examine", "attack_level", "strength_level", "defence_level", "magic_level",
    "ranged_level", "attack_bonus", "strength_bonus", "attack_magic", "magic_bonus",
    "attack_ranged", "ranged_bonus", "defence_stab", "defence_slash", "defence_crush",
    "defence_magic", "defence_ranged",
)


def compact_directory(source: Path, fields: Iterable[str]) -> list[dict[str, Any]]:
    result: list[dict[str, Any]] = []
    for path in sorted(source.glob("*.json"), key=lambda p: int(p.stem)):
        row = json.loads(path.read_text())
        result.append({field: row.get(field) for field in fields})
    return result


def convert_npc_overrides(old_to_new: dict[int, int]) -> dict[str, Any]:
    rows = json.loads((ROOT / "scripts/npc_Def.json").read_text())
    definitions: dict[str, dict[str, Any]] = {}
    collisions: list[dict[str, int]] = []
    for row in rows:
        legacy_id = int(row["id"])
        final_id = old_to_new.get(legacy_id, legacy_id)
        key = str(final_id)
        if key in definitions and definitions[key].get("legacyId") != legacy_id:
            collisions.append({"old": legacy_id, "new": final_id})
        definitions[key] = {
            "legacyId": legacy_id,
            "name": str(row.get("name", "")).replace("_", " "),
            "examine": str(row.get("examine", "")).replace("_", " "),
            "attackAnimation": int(row.get("attackEmote", 806)),
            "deathAnimation": int(row.get("deathEmote", 836)),
            "respawnTicks": int(row.get("respawn", 60)),
            "combatLevel": int(row.get("combat", 0)),
            "size": int(row.get("size", 1)),
            "stats": {
                "attack": int(row.get("attack", 0)),
                "strength": int(row.get("strength", 0)),
                "defence": int(row.get("defence", 0)),
                "hitpoints": int(row.get("hitpoints", 0)),
                "ranged": int(row.get("ranged", 0)),
                "magic": int(row.get("magic", 0)),
            },
        }
    return {"schemaVersion": 1, "collisions": collisions, "definitions": definitions}


def convert_tarnish_npc_definitions(source: Path, old_to_new: dict[int, int]) -> list[dict[str, Any]]:
    converted: list[dict[str, Any]] = []
    for source_row in json.loads(source.read_text()):
        row = dict(source_row)
        legacy_id = int(row["id"])
        row["id"] = old_to_new.get(legacy_id, legacy_id)
        if row["id"] != legacy_id:
            row["legacy-id"] = legacy_id
        converted.append(row)
    return converted


def parse_sql_values(path: Path, table: str) -> list[list[Any]]:
    text = path.read_text()
    match = re.search(rf"INSERT\s+IGNORE\s+INTO\s+{re.escape(table)}\b.*?\bVALUES\s*(.*?);", text, re.I | re.S)
    if not match:
        raise ValueError(f"No INSERT VALUES statement for {table} in {path}")
    source = match.group(1)
    rows: list[list[Any]] = []
    row: list[Any] | None = None
    token: list[str] = []
    quoted = False
    index = 0
    while index < len(source):
        char = source[index]
        if quoted:
            if char == "'" and index + 1 < len(source) and source[index + 1] == "'":
                token.append("'")
                index += 2
                continue
            if char == "'":
                quoted = False
            else:
                token.append(char)
        elif char == "'":
            quoted = True
        elif char == "(":
            row = []
            token = []
        elif char in ",)" and row is not None:
            raw = "".join(token).strip()
            if raw.upper() == "NULL":
                value: Any = None
            elif re.fullmatch(r"-?\d+", raw):
                value = int(raw)
            elif re.fullmatch(r"-?\d+\.\d+", raw):
                value = float(raw)
            else:
                value = raw
            row.append(value)
            token = []
            if char == ")":
                rows.append(row)
                row = None
        elif row is not None and not char.isspace():
            token.append(char)
        index += 1
    return rows


def convert_uber_item_overrides() -> dict[str, Any]:
    columns = [
        "id", "name", "description", "slot", "stackable", "tradeable", "noteable",
        "shopSellValue", "shopBuyValue", "alchemy", "standAnim", "walkAnim", "runAnim",
        "attackAnim", "premium", "twoHanded", "full", "interfaceId",
        *[f"bonus{number}" for number in range(1, 13)],
    ]
    definitions: dict[str, dict[str, Any]] = {}
    for values in parse_sql_values(ROOT / "database/2.1_dodian_default_data.sql", "uber3_items"):
        if len(values) != len(columns):
            raise ValueError(f"Expected {len(columns)} item columns, found {len(values)} for item {values[0]}")
        row = dict(zip(columns, values))
        item_id = int(row.pop("id"))
        row["name"] = str(row["name"]).replace("_", " ")
        row["description"] = str(row["description"]).replace("_", " ")
        row["bonuses"] = [int(row.pop(f"bonus{number}")) for number in range(1, 13)]
        definitions[str(item_id)] = row
    return {"schemaVersion": 1, "definitions": definitions}


def convert_uber_objects() -> dict[str, Any]:
    objects = []
    seen: set[tuple[int, int, int, int]] = set()
    duplicates = 0
    for values in parse_sql_values(ROOT / "database/2_dodian_default_data.sql", "uber3_objects"):
        if len(values) != 4:
            raise ValueError(f"Expected 4 object columns, found {len(values)}")
        if values[0] in (None, 0) or any(value is None for value in values[1:]):
            continue
        object_id, x, y, object_type = (int(value) for value in values)
        key = (object_id, x, y, object_type)
        if key in seen:
            duplicates += 1
            continue
        seen.add(key)
        objects.append({"id": object_id, "x": x, "y": y, "plane": 0, "type": object_type, "rotation": 0})
    if duplicates:
        print(f"Warning: removed {duplicates} duplicate checked-in object override rows")
    return {"schemaVersion": 1, "objects": objects}


def main() -> int:
    parser = argparse.ArgumentParser(description="Import Tarnish definitions and current Uber spawns")
    parser.add_argument("--tarnish-server", type=Path, default=DEFAULT_TARNISH)
    args = parser.parse_args()

    tarnish = args.tarnish_server.resolve()
    old_to_new_path = tarnish / "data/def/npc/oldtonew.txt"
    old_to_new = read_old_to_new(old_to_new_path)

    npc_dir = OUTPUT_ROOT / "npc"
    item_dir = OUTPUT_ROOT / "item"
    world_dir = OUTPUT_ROOT / "world"
    npc_dir.mkdir(parents=True, exist_ok=True)
    item_dir.mkdir(parents=True, exist_ok=True)
    world_dir.mkdir(parents=True, exist_ok=True)

    shutil.copyfile(old_to_new_path, npc_dir / "oldtonew.txt")
    dump_json(
        npc_dir / "tarnish-definitions.json",
        convert_tarnish_npc_definitions(tarnish / "data/def/npc/npc_definitions.json", old_to_new),
    )
    item_definitions = json.loads((tarnish / "data/def/item/item_definitions.json").read_text())
    for row in item_definitions:
        row["id"] = int(row["id"])
    dump_json(item_dir / "tarnish-definitions.json", item_definitions)

    dump_json(npc_dir / "tarnish-monsters.json", {
        "schemaVersion": 1,
        "definitions": compact_directory(tarnish / "data/def/monsters-json", MONSTER_FIELDS),
    })
    dump_json(item_dir / "tarnish-items.json", {
        "schemaVersion": 1,
        "definitions": compact_directory(tarnish / "data/def/items-json", ITEM_FIELDS),
    })
    dump_json(npc_dir / "uber-overrides.json", convert_npc_overrides(old_to_new))
    dump_json(item_dir / "uber-overrides.json", convert_uber_item_overrides())
    dump_json(world_dir / "object-overrides.json", convert_uber_objects())

    report = import_spawns(old_to_new, npc_dir / "spawns")
    dump_json(npc_dir / "migration-report.json", report)
    print(
        f"Imported {report['totalSpawnRows']} spawns across {len(report['families'])} families; "
        f"converted {report['mappedSpawnRows']} rows using oldtonew.txt"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
