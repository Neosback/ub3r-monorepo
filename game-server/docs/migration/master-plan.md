# Migration Master Plan

## Philosophy

- **Cache** = base definitions (read-only, from Tarnish-218)
- **JSON** = content data we control (spawns, overrides, behaviors)
- **MySQL** = only where external tools need it (modcp) or dynamic data
- **Content stays Dodian** — spawns, drops, quests, shops, dialogues are ours

## Data Storage Decisions

| Data | Storage | Why |
|------|---------|-----|
| NPC spawns | JSON | Clean editing, no DB dependency |
| NPC def overrides | JSON | Inline in spawn files |
| NPC behaviors | Kotlin | Behavior modules keyed by NPC ID |
| NPC drops | DB (keep) | Complex querying, future migration possible |
| Object placements | MySQL | Modcp needs to read/write |
| Object def overrides | MySQL | Inline in objects table (extended columns) |
| Item base defs | Cache | `obj.dat`/`obj.idx` — standard OSRS data |
| Item overrides | JSON | Only for items we customize |
| Item shop prices | MySQL | Modcp-friendly, minimal table |
| Player data | MySQL | Persistent, relational, tool-friendly |
| Logging/audit | MySQL | Modcp queries event logs |

## Migration Order

### Phase 1: Foundation (Do First)
Build tooling and reference data:

1. **Cache name index scripts**
   - `scripts/build_npc_name_index.py` → `data/def/npc/cache-name-index.json`
   - `scripts/build_object_name_index.py` → `data/def/object/cache-name-index.json`
   - `scripts/build_item_name_index.py` → `data/def/item/cache-name-index.json`

2. **Tarnish monster data** — copy `data/def/monsters-json/` from Tarnish
   - Provides combat stats, slayer data, attributes for all OSRS NPCs

### Phase 2: NPCs
3. **Script: `scripts/generate_npc_spawns.py`**
   - Reads current Kotlin module spawn coords + old `uber3_npcs` DB stats + `uber-overrides.json`
   - Cross-references against `cache-name-index.json` to determine what needs overrides
   - Outputs `data/def/npc/spawns/*.json` grouped by area

4. **Build `NpcSpawnJsonLoader.kt`**
   - Reads all spawn JSONs at startup
   - Applies inline overrides
   - Registers NPCs in the world

5. **Strip spawn data from Kotlin `NpcModule` files**
   - Convert to pure behavior modules keyed by NPC ID
   - Remove auto-generated spawn files (Cow, Guard, etc.)
   - Delete `NpcSpawnGroups.kt`

6. **Drop DB table: `uber3_npcs`** (or archive it)

### Phase 3: Objects
7. **Update `uber3_objects` table schema** — add plane, rotation, name, size_x, size_y, pathfinding fields

8. **Script: `scripts/migrate_object_ids.py`**
   - For each legacy object ID in `uber3_objects`, look up by name in cache index
   - Update ID to Tarnish ID
   - Flag unmatchable IDs for manual review

9. **Update `ObjectDefinitionRepository.kt`** — read new columns, merge with cache defs at startup

10. **Update `CollisionBuildService`** — apply DB overrides after cache rebuild

### Phase 4: Items
11. **Build live item ID set**
    - Scrape all hardcoded IDs from Kotlin source
    - Extract all IDs from `uber3_drops` table
    - Extract all IDs from shop catalogs
    - Deduplicate → ~600 IDs

12. **Script: `scripts/generate_item_overrides.py`**
    - For each live item ID, pull custom data from `uber3_items` (prices, bonuses, animations, slot)
    - Output `data/def/item/overrides/{id}.json`
    - Skip items where override matches cache default

13. **Build `ItemOverrideRepository.kt`** — loads JSON overrides on top of cache defs

14. **Create minimal `uber3_item_prices` table** — shop prices only (modcp needs)

### Phase 5: Cleanup
15. Delete unused data files
16. Remove old DB tables after verification
17. Write verification tests

## What We Take From Tarnish

We use Tarnish as reference for:
- **Cache format** — already using Tarnish-218 cache
- **Collision system** — already ported
- **JSON formats** — spawns, defs, items-json patterns to mirror
- **Monster stats** — `monsters-json/` for combat data
- **Old-to-new ID mapping** — `oldtonew.txt` for NPC IDs

We do NOT take:
- Tarnish spawn locations
- Tarnish drop tables
- Tarnish dialogues
- Tarnish quest progress
- Tarnish player data

## What Stays Dodian

| Content | Reason |
|---------|--------|
| NPC spawn positions | Our custom world layout |
| Drop tables | Custom rates, custom items |
| Shop catalogs | Custom economies |
| Skill data | Custom leveling rates, custom behaviors |
| Quests/minigames | Fully custom |
| Dialogues | Custom NPC interactions |
| Player data | Live production database |

## File Tree After Migration

```
game-server/
  data/
    cache/                    # Tarnish-218 cache (unchanged)
    def/
      npc/
        cache-name-index.json # Auto-generated from cache
        spawns/
          lumbridge.json      # Spawns + inline overrides
          varrock.json
          ...
      object/
        cache-name-index.json # Auto-generated from cache
      item/
        cache-name-index.json # Auto-generated from cache
        overrides/
          4151.json           # Only customized items
          995.json
          ...
  src/.../
    engine/
      systems/
        cache/                # Cache decoders (unchanged)
        npc/
          NpcSpawnJsonLoader.kt   # NEW
          NpcBehaviorRegistry.kt  # NEW (moved from old modules)
        objects/
          ObjectDefinitionRepository.kt  # UPDATED
          CollisionBuildService.kt       # UPDATED
        items/
          ItemOverrideRepository.kt      # NEW
    npc/
      BankerBehavior.kt       # Behaviors only, no spawns
      CowBehavior.kt
      ...
    persistence/
      world/
        ObjectDefinitionRepository.kt    # UPDATED
```
