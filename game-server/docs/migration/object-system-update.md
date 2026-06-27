# Object System Update

## Policy Decision

Objects stay in **MySQL** (`uber3_objects` table) because the external moderation panel (modcp) needs to read/write custom object placements. This is a case where DB makes sense — objects are admin-managed content that an external tool needs to query.

## What Needs to Change

### Table Schema

Current (`uber3_objects`):
```sql
id   INT(4) UNSIGNED,   -- object ID (legacy Dodian ID)
x    INT(4) UNSIGNED,
y    INT(4),
type INT(1)             -- object type
```

Missing critical fields for pathfinding and rendering. New schema:

```sql
ALTER TABLE uber3_objects
  ADD COLUMN plane    INT DEFAULT 0,
  ADD COLUMN rotation INT DEFAULT 0,
  ADD COLUMN name     VARCHAR(255) DEFAULT NULL,
  ADD COLUMN size_x   INT DEFAULT 1,
  ADD COLUMN size_y   INT DEFAULT 1,
  ADD COLUMN solid    BOOLEAN DEFAULT TRUE,
  ADD COLUMN walkable BOOLEAN DEFAULT FALSE,
  ADD COLUMN block_walk INT DEFAULT 2,
  ADD COLUMN block_range BOOLEAN DEFAULT TRUE,
  ADD COLUMN has_actions BOOLEAN DEFAULT TRUE;
```

### ID Migration

The current `uber3_objects` table uses legacy Dodian object IDs. We need to map them to Tarnish cache IDs.

**Method: Name-based cross-reference**
1. Build a cache name index (`data/def/object/cache-name-index.json`) from the Tarnish `loc.dat`
2. For each legacy object ID in `uber3_objects`, look up its name from the old SQL's item/object tables
3. Match that name against the cache name index to find the new Tarnish ID
4. Update the `id` column in `uber3_objects`

If a legacy object ID doesn't have a known name, flag it for manual review.

### Runtime Loading at Startup

`ObjectDefinitionRepository.kt` loads from MySQL at startup. Update it to read the new columns:

```kotlin
object ObjectDefinitionRepository {
    data class ObjectOverride(
        val id: Int, val x: Int, val y: Int, val plane: Int,
        val type: Int, val rotation: Int,
        val name: String?, val sizeX: Int?, val sizeY: Int?,
        val solid: Boolean?, val walkable: Boolean?,
        val blockWalk: Int?, val blockRange: Boolean?,
        val hasActions: Boolean?,
    )

    fun loadObjects(): List<ObjectOverride> =
        DbAsyncRepository.withConnection { connection ->
            connection.createStatement().use { stmt ->
                stmt.executeQuery("""
                    SELECT id, x, y, plane, type, rotation, name,
                           size_x, size_y, solid, walkable,
                           block_walk, block_range, has_actions
                    FROM ${DbTables.GAME_OBJECT_DEFINITIONS}
                """).use { results ->
                    val list = ArrayList<ObjectOverride>()
                    while (results.next()) {
                        list += ObjectOverride(
                            id = results.getInt("id"),
                            x = results.getInt("x"),
                            y = results.getInt("y"),
                            plane = results.getInt("plane"),
                            type = results.getInt("type"),
                            rotation = results.getInt("rotation"),
                            name = results.getString("name"),
                            sizeX = results.getInt("size_x").takeIf { !it.wasNull() },
                            sizeY = results.getInt("size_y").takeIf { !it.wasNull() },
                            solid = results.getBoolean("solid").takeIf { !it.wasNull() },
                            walkable = results.getBoolean("walkable").takeIf { !it.wasNull() },
                            blockWalk = results.getInt("block_walk").takeIf { !it.wasNull() },
                            blockRange = results.getBoolean("block_range").takeIf { !it.wasNull() },
                            hasActions = results.getBoolean("has_actions").takeIf { !it.wasNull() },
                        )
                    }
                    list
                }
            }
        }
}
```

### Collision Integration

After `CollisionBuildService.rebuild()` runs from cache data, apply DB overrides:

1. For each DB object override, call `removeObject()` first to clear the cache-based collision at that tile
2. Merge the DB override fields with the cache `GameObjectData` definition
3. Call `applyObject()` with the merged data

```kotlin
fun applyDbOverrides() {
    val overrides = ObjectDefinitionRepository.loadObjects()
    for (obj in overrides) {
        val cacheDef = GameObjectData.forId(obj.id)
        val merged = cacheDef.toBuilder().apply {
            obj.name?.let { name = it }
            obj.sizeX?.let { sizeX = it }
            obj.sizeY?.let { sizeY = it }
            obj.solid?.let { solid = it }
            obj.walkable?.let { walkable = it }
            obj.blockWalk?.let { blockWalk = it }
            obj.blockRange?.let { blockRange = it }
            obj.hasActions?.let { hasActionsFlag = it }
        }
        removeObject(/* ... */)  // clear tile
        applyObject(/* ... */)   // re-apply with merged data
    }
}
```

### Adding Objects at Runtime

The existing `::spawnobject` command (`DevSpawnAndNpcCommands.kt`) already inserts into `uber3_objects`. Update it to also accept override fields:

```
::spawnobject 24071 x y type rotation --name "Bank booth" --solid true --block_walk 2
```

This keeps the modcp workflow intact while adding pathfinding data.

## Summary

| Piece | Storage | How |
|-------|---------|-----|
| Object placements | MySQL | Updated schema with override fields |
| Base definitions | Cache | `loc.dat`/`loc.idx` via `ObjectDefinitionDecoder` |
| Runtime merge | CollisionBuildService | Cache def → DB override merge at startup |
| Admin editing | MySQL / modcp | Existing tools continue to work |
