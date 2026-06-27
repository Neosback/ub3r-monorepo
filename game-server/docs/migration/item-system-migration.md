# Item System Migration

## Problem

The current `uber3_items` DB table is a dump of ~11,500 items with mostly default/wrong data — copied from some old source, never properly curated. Only the items we actually use have meaningful overrides (prices, bonuses, animations). The rest is dead weight.

## Goal

Migrate to Tarnish's pattern: **cache base defs + JSON override files**. Only maintain data for items we actually reference.

## Where Items Come From (Tarnish Reference)

Tarnish uses two layers that override each other:
1. **`data/def/item/item_definitions.json`** — a single large curated file with equipment bonuses, animations, ranged defs
2. **`data/def/items-json/{id}.json`** — OSRS Wiki dump, one file per item, overrides the legacy file

Neither layer uses the cache `obj.dat`/`obj.idx` for items.

**Our approach**: Since we're already running a Tarnish-218 cache, we should load item base definitions from the cache (like we do for objects) and layer our custom overrides on top via JSON files for only the items we actually use.

## Determining "Used" Items

Cross-reference these sources to build the live set:

1. **Shop catalogs** — all item IDs in every shop definition (~150 unique IDs)
2. **Skill data files** — mining/ores, smithing products, woodcutting logs, fishing catches, cooking recipes, crafting materials, herblore ingredients, farming seeds/harvest, fletching components, runecrafting items
3. **NPC drops** — all item IDs in `uber3_drops` table
4. **Quest/item interactions** — hardcoded item checks in Kotlin source
5. **Starter items** — items given on account creation
6. **Item-on-item/object interactions** — crafting combos, tool usage
7. **Equipment** — items players can wear (referenced by combat code)

From the source scan: approximately **600 unique item IDs** are hardcoded across the codebase. This is the "live set."

## Migration Strategy

### Step 1: Build Item Name Index

Extract all item IDs + names from the Tarnish cache (`obj.dat`/`obj.idx`):

```bash
# Script output: data/def/item/cache-name-index.json
{
  "995": { "name": "Coins", "stackable": true, "noted": false, "linked_id_noted": 996 },
  "4151": { "name": "Abyssal whip", "stackable": false, "equipable": true },
  ...
}
```

### Step 2: Cross-Reference Live IDs Against Cache

For each of the ~600 live item IDs:
- If the item exists in the cache → use the cache as the base definition
- If the item does NOT exist in the cache → it's a custom Dodian item, flag for manual entry

### Step 3: Generate Override Files

For each live item ID, extract custom data from the old `uber3_items` table and write it to `data/def/item/overrides/{id}.json`:

```json
{
  "id": 4151,
  "name": "Abyssal whip",
  "bonuses": [0, 82, 0, 0, 0, 0, 0, 0, 0, 0, 82, 0, 0, 0],
  "slot": "weapon",
  "requirements": { "attack": 70 },
  "twohanded": false,
  "shop": {
    "buyValue": 120001,
    "sellValue": 60000
  },
  "alchemy": {
    "low": 48000,
    "high": 72000
  },
  "animations": {
    "stand": 1832,
    "walk": 1833,
    "run": 1834,
    "attack": 1658,
    "block": 1659
  }
}
```

The override contains ONLY what differs from the cache definition. Most items won't need an override file at all — only the ~200 that have custom prices, bonuses, or animations.

### Step 4: New Runtime Loader

Create `ItemOverrideRepository.kt`:

```kotlin
object ItemOverrideRepository {
    private val overrides = ConcurrentHashMap<Int, ItemOverride>()

    fun load() {
        val dir = File("data/def/item/overrides/")
        if (!dir.exists()) return
        for (file in dir.listFiles()!!) {
            if (file.extension != "json") continue
            val id = file.nameWithoutExtension.toIntOrNull() ?: continue
            val override = gson.fromJson(file.bufferedReader(), ItemOverride::class.java)
            overrides[id] = override
        }
    }

    fun forId(id: Int): ItemOverride? = overrides[id]

    fun merged(id: Int): ItemDefinition {
        val cache = ItemCacheDefinitionDecoder.forId(id)  // from obj.dat
        val override = overrides[id]
        return if (override != null) cache.merge(override) else cache
    }
}
```

### Step 5: Retire `uber3_items` Table

Once migration is complete, the `uber3_items` table can be:
- **Kept** for backward compatibility (if external tools read it)
- **Dropped** and replaced with a read-only view that queries the JSON files
- **Reduced** to only contain items that have custom pricing (for modcp editing)

### Recommendation

Keep a minimal `uber3_items` table for admin-editable fields (shop prices) that the modcp needs to read/write:

```sql
CREATE TABLE uber3_item_prices (
    id INT PRIMARY KEY,
    buy_price INT,
    sell_price INT,
    low_alch INT,
    high_alch INT
);
```

This is the only item data the modcp needs to touch. Everything else (bonuses, animations, slot, requirements) lives in JSON.

## What If an Item Doesn't Exist in the Cache?

Some custom Dodian items (like holiday items, custom gear) might not exist in the Tarnish-218 cache. For these:
1. Add a full definition override that includes name, examine, model data
2. If the item needs a client model, we'd need to inject it into the cache (complex) or use a client-side override

For now, any custom item without a cache entry gets a full override JSON with all fields populated.

## Summary

| Piece | Storage | How |
|-------|---------|-----|
| Base defs | Cache | `obj.dat`/`obj.idx` at startup |
| Overrides | JSON | `data/def/item/overrides/{id}.json` |
| Shop prices | MySQL | Minimal table for modcp |
| All other fields | JSON | Bonuses, animations, slot, requirements |
| Cache name index | JSON | `data/def/item/cache-name-index.json` (one-time build) |
