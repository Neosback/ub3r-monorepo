#!/usr/bin/env python3
import json
import re
from collections import defaultdict
from pathlib import Path


REPO = Path(__file__).resolve().parents[2]
NPC_DIR = REPO / "game-server/src/main/kotlin/net/dodian/uber/game/npc"
DATA = Path("/Users/tylercovalt/Desktop/tarnish-main/game-server/data")
SPAWN_JSON = DATA / "Ubers mysql as json export/npc_Spawn.json"
DEF_JSON = DATA / "Ubers mysql as json export/npc_Def.json"
OLD_TO_NEW = DATA / "def/npc/oldtonew.txt"
MONSTERS = DATA / "def/monsters-json"

CORE_FILES = {
    "NpcContent.kt",
    "NpcDefinitionOverride.kt",
    "NpcDefinitionRepository.kt",
    "NpcFamilyDsl.kt",
    "NpcSpawnDef.kt",
}

HAND_AUTHORED_BY_FINAL_ID = {
    555: "ShopKeeper.kt",
    557: "Wydin.kt",
    2813: "ShopKeeper.kt",
    10681: "Aubury.kt",
    1306: "MakeoverMage.kt",
}

HAND_AUTHORED_FILES = set(HAND_AUTHORED_BY_FINAL_ID.values()) | {
    "DukeHoracio.kt",
    "PartyPete.kt",
    "Gundai.kt",
    "Watcher.kt",
    "HerbloreNpcDialogue.kt",
    "SlayerMasterDialogue.kt",
}

KOTLIN_KEYWORDS = {
    "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
    "if", "in", "interface", "is", "null", "object", "package", "return",
    "super", "this", "throw", "true", "try", "typealias", "typeof", "val",
    "var", "when", "while",
}


def load_old_to_new():
    pairs = {}
    for line in OLD_TO_NEW.read_text(errors="ignore").splitlines():
        nums = re.findall(r"-?\d+", line)
        if len(nums) >= 2:
            pairs[int(nums[0])] = int(nums[1])
    return pairs


def clean_name(value):
    if value is None:
        return None
    value = str(value).replace("_", " ").strip()
    if not value or value.lower() in {"no name", "null", "none"}:
        return None
    if "fuck" in value.lower():
        return None
    return re.sub(r"\s+", " ", value)


def kotlin_string(value):
    return json.dumps(value)


def class_name(name, fallback_id):
    words = re.findall(r"[A-Za-z0-9]+", name or "")
    if not words:
        return f"UnknownNpc{fallback_id}"
    result = "".join(w[:1].upper() + w[1:] for w in words)
    if result[0].isdigit():
        result = f"Npc{result}"
    if result in KOTLIN_KEYWORDS:
        result += "Npc"
    return result


def int_value(row, key, default=0):
    value = row.get(key, default)
    if value is None or value == "":
        return default
    return int(value)


def positive(row, key):
    value = int_value(row, key, 0)
    return value if value > 0 else None


def load_monsters():
    monsters = {}
    if not MONSTERS.exists():
        return monsters
    for path in MONSTERS.glob("*.json"):
        try:
            npc_id = int(path.stem)
        except ValueError:
            continue
        try:
            monsters[npc_id] = json.loads(path.read_text())
        except json.JSONDecodeError:
            continue
    return monsters


def best_name(final_id, old_rows, defs_by_id, monsters):
    for old_id, _spawn in old_rows:
        name = clean_name(defs_by_id.get(old_id, {}).get("name"))
        if name:
            return name
    name = clean_name(defs_by_id.get(final_id, {}).get("name"))
    if name:
        return name
    name = clean_name(monsters.get(final_id, {}).get("name"))
    if name:
        return name
    return f"UnknownNpc{final_id}"


def choose_export_def(final_id, old_rows, defs_by_id):
    for old_id, _spawn in old_rows:
        row = defs_by_id.get(old_id)
        if row:
            return row
    return defs_by_id.get(final_id, {})


FACE_NAMES = {
    0: "NORTH",
    1: "NORTH_EAST",
    2: "EAST",
    3: "SOUTH_EAST",
    4: "SOUTH",
    5: "SOUTH_WEST",
    6: "WEST",
    7: "NORTH_WEST",
}


def cache_lines(final_id, export_def, old_rows):
    fields = []
    old_ids = {old_id for old_id, _spawn in old_rows}
    name = clean_name(export_def.get("name"))
    examine = clean_name(export_def.get("examine"))
    # Name is server-visible in dialogue/chat contexts, not the client's right-click menu.
    if name and (old_ids != {final_id} or final_id not in old_ids):
        fields.append(("name", kotlin_string(name)))
    if examine:
        fields.append(("examine", kotlin_string(examine)))
    if not fields:
        return []
    body = ["    cache {"]
    for key, value in fields:
        body.append(f"        {key} = {value}")
    body.append("    }")
    return body


def server_lines(final_id, export_def, monster, primary_id):
    fields = []
    simple_map = [
        ("attackAnimation", "attackEmote"),
        ("deathAnimation", "deathEmote"),
        ("respawnTicks", "respawn", 60),
        ("attack", "attack"),
        ("strength", "strength"),
        ("defence", "defence"),
        ("hitpoints", "hitpoints"),
        ("ranged", "ranged"),
        ("magic", "magic"),
    ]
    for entry in simple_map:
        if len(entry) == 3:
            target, source, default = entry
        else:
            target, source = entry
            default = 0
        value = positive(export_def, source)
        if source == "attackEmote" and value == 806:
            value = None
        if source == "deathEmote" and value == 836:
            value = None
        if value == default:
            value = None
        if value is not None:
            fields.append((target, str(value)))

    if monster:
        monster_map = [
            ("hitpoints", "hitpoints"),
            ("attack", "attack_level"),
            ("strength", "strength_level"),
            ("defence", "defence_level"),
            ("magic", "magic_level"),
            ("ranged", "ranged_level"),
        ]
        existing = {name for name, _value in fields}
        for target, source in monster_map:
            if target in existing:
                continue
            value = monster.get(source)
            if isinstance(value, int) and value > 0:
                fields.append((target, str(value)))
                existing.add(target)

    if not fields:
        return []
    body = ["    server {" if final_id == primary_id else "    server(%d) {" % final_id]
    for key, value in fields:
        body.append(f"        {key} = {value}")
    body.append("    }")
    return body


def unsupported_server_values(export_def, monster):
    values = {}
    combat = positive(export_def, "combat")
    if combat is not None:
        values["combatLevel"] = combat
    if monster:
        scalar_fields = {
            "attackSpeed": "attack_speed",
            "strengthBonus": "strength_bonus",
            "rangedBonus": "ranged_bonus",
            "magicBonus": "magic_bonus",
            "stabDefenceBonus": "defence_stab",
            "slashDefenceBonus": "defence_slash",
            "crushDefenceBonus": "defence_crush",
            "magicDefenceBonus": "defence_magic",
            "rangedDefenceBonus": "defence_ranged",
        }
        for target, source in scalar_fields.items():
            value = monster.get(source)
            if isinstance(value, int) and value != 0:
                values[target] = value
        bool_fields = {
            "aggressive": "aggressive",
            "poisonImmune": "immune_poison",
            "venomImmune": "immune_venom",
        }
        for target, source in bool_fields.items():
            if monster.get(source) is True:
                values[target] = True
    return values


def spawn_call(primary_id, final_id, row):
    args = [str(int_value(row, "x")), str(int_value(row, "y"))]
    z = int_value(row, "height")
    face = int_value(row, "face")
    if face not in FACE_NAMES:
        raise ValueError(f"Unsupported NPC face value {face} for id={final_id} x={args[0]} y={args[1]}")
    if z != 0:
        args.append(f"z = {z}")
    if face != 0:
        args.append(f"face = {FACE_NAMES[face]}")
    prefix = "spawn" if final_id == primary_id else f"spawnId({final_id}, "
    if final_id == primary_id:
        return f"        {prefix}({', '.join(args)})"
    return f"        {prefix}{', '.join(args)})"


def write_family(path, object_name, family_name, primary_id, ids, cache_overrides, server_definitions, spawn_lines):
    lines = [
        "package net.dodian.uber.game.npc",
        "",
        f"internal object {object_name} : NpcFamily by npcFamily({kotlin_string(family_name)}, {primary_id}, block = {{",
    ]
    extra_ids = [i for i in ids if i != primary_id]
    if extra_ids:
        lines.append(f"    ids({', '.join(map(str, extra_ids))})")
        lines.append("")
    if cache_overrides:
        for index, block in enumerate(cache_overrides):
            if index:
                lines.append("")
            lines.extend(block)
        lines.append("")
    if server_definitions:
        for index, block in enumerate(server_definitions):
            if index:
                lines.append("")
            lines.extend(block)
        lines.append("")
    lines.append("    spawns {")
    lines.extend(spawn_lines)
    lines.append("    }")
    lines.append("})")
    lines.append("")
    path.write_text("\n".join(lines))


def main():
    NPC_DIR.mkdir(parents=True, exist_ok=True)
    for path in NPC_DIR.glob("*.kt"):
        if path.name not in CORE_FILES and path.name not in HAND_AUTHORED_FILES:
            path.unlink()

    old_to_new = load_old_to_new()
    spawns = json.loads(SPAWN_JSON.read_text())
    defs_by_id = {int(row["id"]): row for row in json.loads(DEF_JSON.read_text())}
    monsters = load_monsters()

    grouped = defaultdict(list)
    for row in spawns:
        if int_value(row, "live") != 1:
            continue
        old_id = int_value(row, "id")
        final_id = old_to_new.get(old_id, old_id)
        grouped[final_id].append((old_id, row))

    used_names = set(CORE_FILES)
    audit = []
    for final_id in sorted(grouped):
        if final_id in HAND_AUTHORED_BY_FINAL_ID:
            for old_id, row in grouped[final_id]:
                audit.append({
                    "oldId": old_id,
                    "finalId": final_id,
                    "name": best_name(final_id, grouped[final_id], defs_by_id, monsters),
                    "familyFile": HAND_AUTHORED_BY_FINAL_ID[final_id],
                    "status": "hand-authored",
                    "x": int_value(row, "x"),
                    "y": int_value(row, "y"),
                    "z": int_value(row, "height"),
                    "face": FACE_NAMES.get(int_value(row, "face"), int_value(row, "face")),
                })
            continue
        old_rows = sorted(grouped[final_id], key=lambda item: (int_value(item[1], "x"), int_value(item[1], "y"), int_value(item[1], "height")))
        family_name = best_name(final_id, old_rows, defs_by_id, monsters)
        object_name = class_name(family_name, final_id)
        base_object_name = object_name
        suffix = 2
        while f"{object_name}.kt" in used_names:
            object_name = f"{base_object_name}{suffix}"
            suffix += 1
        used_names.add(f"{object_name}.kt")

        ids = [final_id]
        export_def = choose_export_def(final_id, old_rows, defs_by_id)
        monster = monsters.get(final_id)
        cache_blocks = [cache_lines(final_id, export_def, old_rows)]
        cache_blocks = [block for block in cache_blocks if block]
        server_blocks = [server_lines(final_id, export_def, monster, final_id)]
        server_blocks = [block for block in server_blocks if block]
        unsupported_values = unsupported_server_values(export_def, monster)

        seen = set()
        spawn_lines = []
        for _old_id, row in old_rows:
            key = (final_id, int_value(row, "x"), int_value(row, "y"), int_value(row, "height"))
            if key in seen:
                continue
            seen.add(key)
            spawn_lines.append(spawn_call(final_id, final_id, row))
            audit.append({
                "oldId": _old_id,
                "finalId": final_id,
                "name": family_name,
                "x": key[1],
                "y": key[2],
                "z": key[3],
                "face": FACE_NAMES.get(int_value(row, "face"), int_value(row, "face")),
                "moveChance": int_value(row, "movechance"),
                "unsupportedRuntimeValues": unsupported_values,
            })

        write_family(
            NPC_DIR / f"{object_name}.kt",
            object_name,
            family_name,
            final_id,
            ids,
            cache_blocks,
            server_blocks,
            spawn_lines,
        )

    report = REPO / "game-server/build/reports/npc-family-import.json"
    report.parent.mkdir(parents=True, exist_ok=True)
    report.write_text(json.dumps({"spawns": audit, "count": len(audit), "families": len(grouped)}, indent=2))
    print(f"wrote {len(grouped)} families and {len(audit)} spawns")


if __name__ == "__main__":
    main()
