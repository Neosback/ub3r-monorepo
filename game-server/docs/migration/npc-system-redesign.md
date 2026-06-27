# NPC System Redesign

## Data Flow (Final)

```
Cache (npc.dat/npc.idx)
  └── Base animations, names, sizes (fallback only)

data/def/npc/spawns/*.json
  └── Each spawn file = region/area grouping
      └── Per-spawn entries with inline def overrides
          ├── id (Tarnish NPC ID)
          ├── position { x, y, height }
          ├── facing, radius
          └── overrides (optional block)
              ├── name, examine, size
              ├── combatLevel, attackable, aggressive
              ├── attackAnimation, deathAnimation
              ├── stats { attack, strength, defence, hitpoints, ranged, magic }
              └── respawnTicks, walkRadius, attackRange, alwaysActive

DB (uber3_npcs) ─── ELIMINATED for spawns/defs
DB (uber3_drops) ─── KEPT for drops (or migrated to JSON)
```

## Spawn JSON Format

One file per logical group (region, activity, etc.):

```json
{
  "schemaVersion": 2,
  "group": "lumbridge",
  "spawns": [
    {
      "id": 1613,
      "position": { "x": 3208, "y": 3218, "height": 0 },
      "facing": "SOUTH",
      "radius": 3,
      "overrides": {
        "name": "Banker",
        "combatLevel": 0,
        "size": 1,
        "attackAnimation": 806,
        "deathAnimation": 2304,
        "respawnTicks": 8,
        "actions": ["Walk here", "Talk-to", "Bank"]
      }
    },
    {
      "id": 2805,
      "position": { "x": 2604, "y": 3114, "height": 0 },
      "radius": 5,
      "overrides": {
        "stats": {
          "attack": 1,
          "strength": 1,
          "defence": 1,
          "hitpoints": 8,
          "ranged": 0,
          "magic": 0
        }
      }
    }
  ]
}
```

The `overrides` block is entirely optional. If omitted, the cache definition is used as-is. This keeps spawn files minimal — only override what differs from the cache.

### Generating the Initial Overrides

To build the initial JSON spawn files, cross-reference:

1. **Current Dodian spawns** (from existing Kotlin modules + `data/def/npc/uber-overrides.json`)
2. **Tarnish cache definitions** (`npc.dat`/`npc.idx`) for base animations/names
3. **Old `uber3_npcs` DB data** for custom stats

For each spawned NPC ID:
- If cache has correct name + stats → no override needed
- If cache name/stats differ from Dodian's custom values → populate override block
- If ID doesn't exist in cache at all (custom NPC) → full override

### Migration Steps

1. **Extract all unique spawned NPC IDs** from every `NpcSpawnDef` in the Kotlin modules
2. **Script** (`scripts/generate_npc_spawn_json.py`) reads:
   - Current Kotlin module spawns → positions
   - `uber-overrides.json` → custom def data
   - `uber3_npcs` DB dump → stat overrides  
   - Tarnish cache → base defs for comparison
3. **Output** grouped JSON files under `data/def/npc/spawns/`
4. **Replace all Kotlin `NpcModule` files** with the new JSON loader
5. **New loader** `NpcSpawnJsonLoader.kt` reads all spawn JSONs at startup, applies overrides, registers NPCs

### What Happens to Kotlin Module Behaviors?

NPC behaviors (dialog, trading, quest interactions) need a new home. Options:
- **Keep Kotlin modules** but strip spawn data — they become pure behavior-only modules keyed by NPC ID
- **Move to a JSON script system** (complex, future work)
- **Register behaviors via annotation** on the original Kotlin files, separate from spawn data

Recommended: Kotlin behavior modules keep their dialog/option handlers but lose inline spawn entries. The spawn JSON provides all location + override data. The behavior system looks up behaviors by NPC ID from the Kotlin modules.

## NPC Behavior Modules (Post-Redesign)

```kotlin
// Banker.kt — behaviors only, no spawns
@NpcBehavior(npcIds = [1613, 3094, 395, 7677])
object BankerBehavior : NpcBehaviorModule {
    override fun onTalkTo(client: Client, npc: Npc) {
        npc("Good day, how can I help you?")
        choice("What would you like to say?") {
            "I'd like to access my bank account, please." {
                finishThen { openBank() }
            }
        }
    }
}
```

## Summary

| Piece | Storage | How |
|-------|---------|-----|
| Spawn positions | JSON | `data/def/npc/spawns/*.json` |
| Def overrides | JSON | Inline in spawn entries |
| Behaviors | Kotlin | Modules keyed by NPC ID |
| Drops | DB (or JSON) | Keep DB for now, migrate later |
| Cache defs | Cache | Fallback for anything not overridden |
