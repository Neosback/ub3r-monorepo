@file:Suppress("UNUSED_PARAMETER")

package net.dodian.uber.game.engine.systems.cache

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.engine.routing.CollisionDirection
import net.dodian.uber.game.engine.routing.WorldRouteService

class CollisionBuildService(
    private val collision: WorldRouteService = WorldRouteService,
    private val skippedObjectKeys: Set<Long> = emptySet(),
) {
    fun clear() {
        collision.clear()
    }

    fun rebuild(table: MapIndexTable) {
        collision.clear()
        applyTerrain(table)
        applyObjects(table)
    }

    fun applyTerrain(table: MapIndexTable) {
        for ((_, grid) in table.tileGrids) {
            applyTerrain(grid)
        }
    }

    fun applyTerrain(grid: DecodedMapTileGrid) {
        applyTerrain(grid, CollisionPlaneResolver.from(grid))
    }

    fun applyTerrain(grid: DecodedMapTileGrid, planeResolver: CollisionPlaneResolver) {
        val regionBaseX = (grid.regionId shr 8) * 64
        val regionBaseY = (grid.regionId and 0xFF) * 64
        for (plane in 0 until 4) {
            collision.allocateRegionPlane(regionBaseX, regionBaseY, plane)
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    val globalX = regionBaseX + x
                    val globalY = regionBaseY + y
                    val tile = grid.getTile(x, y, plane)
                    if (tile.isBridge()) {
                        collision.markBridge(globalX, globalY, plane)
                    }
                    if (!tile.isBlocked()) {
                        continue
                    }

                    val effectivePlane = planeResolver.terrainPlane(x, y, plane)
                    if (effectivePlane < 0) {
                        continue
                    }

                    collision.markTerrainBlocked(globalX, globalY, effectivePlane)
                }
            }
        }
    }

    fun applyObjects(table: MapIndexTable) {
        val planeResolversByRegion = table.tileGrids.mapValues { (_, grid) -> CollisionPlaneResolver.from(grid) }
        for (obj in table.objects) {
            applyObjectDataResolved(
                obj = obj,
                definition = GameObjectData.forId(obj.objectId),
                planeResolver = planeResolversByRegion[obj.regionId],
            )
        }
    }

    fun applyObjects(objects: List<DecodedMapObject>) {
        for (obj in objects) {
            applyObjectData(obj, GameObjectData.forId(obj.objectId))
        }
    }

    fun applyObjectData(obj: DecodedMapObject, definition: GameObjectData, grid: DecodedMapTileGrid? = null) {
        applyObjectDataResolved(obj, definition, grid?.let { CollisionPlaneResolver.from(it) })
    }

    fun applyObjectDataResolved(obj: DecodedMapObject, definition: GameObjectData, planeResolver: CollisionPlaneResolver?) {
        val effectivePlane = adjustedPlane(obj.x, obj.y, obj.plane, planeResolver)
        if (effectivePlane < 0 || isSkippedObject(obj.x, obj.y, effectivePlane)) {
            return
        }
        val impenetrable = if (obj.objectId in BLOCK_RANGE_FALSE_OVERRIDES) false else definition.isImpenetrable()
        applyObject(
            id = obj.objectId,
            x = obj.x,
            y = obj.y,
            z = effectivePlane,
            type = obj.type,
            rotation = obj.rotation,
            sizeX = definition.sizeX,
            sizeY = definition.sizeY,
            solid = definition.isSolid(),
            blockWalk = definition.blockWalk(),
            impenetrable = impenetrable,
            breakRouteFinding = definition.breakRouteFinding(),
            hasActions = definition.hasActions(),
            decoration = definition.isDecoration(),
        )
    }

    fun applyTerrainAndObjects(grid: DecodedMapTileGrid?, objects: List<DecodedMapObject>) {
        val planeResolver = grid?.let { CollisionPlaneResolver.from(it) }
        if (grid != null) {
            applyTerrain(grid, planeResolver!!)
        }
        for (obj in objects) {
            applyObjectDataResolved(obj, GameObjectData.forId(obj.objectId), planeResolver)
        }
    }

    fun applyObject(
        id: Int,
        x: Int,
        y: Int,
        z: Int,
        type: Int,
        rotation: Int,
        sizeX: Int,
        sizeY: Int,
        solid: Boolean,
        walkable: Boolean = !solid,
        hasActions: Boolean = true,
        objectName: String? = null,
        blockWalk: Int = if (solid) 2 else 0,
        blockRange: Boolean = blockWalk != 0,
        breakRouteFinding: Boolean = false,
        grid: DecodedMapTileGrid? = null,
        impenetrable: Boolean = blockRange,
        decoration: Boolean = false,
    ) = updateObjectCollision(
        remove = false,
        x = x,
        y = y,
        z = adjustedPlane(x, y, z, grid),
        type = type,
        rotation = rotation,
        sizeX = sizeX,
        sizeY = sizeY,
        solid = solid,
        blockWalk = blockWalk,
        impenetrable = impenetrable,
        breakRouteFinding = breakRouteFinding,
        hasActions = hasActions,
        decoration = decoration,
    )

    fun removeObject(
        id: Int,
        x: Int,
        y: Int,
        z: Int,
        type: Int,
        rotation: Int,
        sizeX: Int,
        sizeY: Int,
        solid: Boolean,
        walkable: Boolean = !solid,
        hasActions: Boolean = true,
        objectName: String? = null,
        blockWalk: Int = if (solid) 2 else 0,
        blockRange: Boolean = blockWalk != 0,
        breakRouteFinding: Boolean = false,
        grid: DecodedMapTileGrid? = null,
        impenetrable: Boolean = blockRange,
        decoration: Boolean = false,
    ) = updateObjectCollision(
        remove = true,
        x = x,
        y = y,
        z = adjustedPlane(x, y, z, grid),
        type = type,
        rotation = rotation,
        sizeX = sizeX,
        sizeY = sizeY,
        solid = solid,
        blockWalk = blockWalk,
        impenetrable = impenetrable,
        breakRouteFinding = breakRouteFinding,
        hasActions = hasActions,
        decoration = decoration,
    )

    private fun updateObjectCollision(
        remove: Boolean,
        x: Int,
        y: Int,
        z: Int,
        type: Int,
        rotation: Int,
        sizeX: Int,
        sizeY: Int,
        solid: Boolean,
        blockWalk: Int,
        impenetrable: Boolean,
        breakRouteFinding: Boolean,
        hasActions: Boolean,
        decoration: Boolean,
    ) {
        if (z < 0 || blockWalk == 0) {
            return
        }

        val normalizedRotation = rotation and 0x3
        val (width, length) = resolveFootprint(normalizedRotation, sizeX, sizeY)
        val add = !remove

        when {
            type == 22 -> {
                if (blockWalk == 1) collision.markGroundDecoration(z, x, y, add)
            }
            type == 10 || type == 11 || type == 9 || type >= 12 -> {
                collision.markOccupant(z, x, y, width, length, impenetrable, breakRouteFinding, add)
            }
            type in 0..3 -> {
                val orientation = CollisionDirection.WNES[normalizedRotation]
                if (add) {
                    collision.markWall(orientation, z, x, y, type, impenetrable, add = true)
                } else {
                    collision.markWall(orientation, z, x, y, type, impenetrable, add = false)
                }
            }
        }
    }

    fun auditObject(obj: DecodedMapObject, grid: DecodedMapTileGrid?): CacheCollisionAuditObject =
        auditObjectResolved(obj, grid?.let { CollisionPlaneResolver.from(it) })

    fun auditObjectResolved(obj: DecodedMapObject, planeResolver: CollisionPlaneResolver?): CacheCollisionAuditObject {
        val effectivePlane = adjustedPlane(obj.x, obj.y, obj.plane, planeResolver)
        val skippedReason =
            when {
                effectivePlane < 0 -> "plane_underflow"
                isSkippedObject(obj.x, obj.y, effectivePlane) -> "tarnish_removed_object"
                else -> null
            }
        return CacheCollisionAuditObject(
            objectId = obj.objectId,
            x = obj.x,
            y = obj.y,
            rawPlane = obj.plane,
            effectivePlane = effectivePlane,
            type = obj.type,
            rotation = obj.rotation,
            regionId = obj.regionId,
            skippedReason = skippedReason,
        )
    }

    private fun adjustedPlane(x: Int, y: Int, plane: Int, grid: DecodedMapTileGrid?): Int {
        if (grid == null) {
            return plane
        }
        return adjustedPlane(x, y, plane, CollisionPlaneResolver.from(grid))
    }

    private fun adjustedPlane(x: Int, y: Int, plane: Int, planeResolver: CollisionPlaneResolver?): Int {
        if (planeResolver == null) {
            return plane
        }
        val localX = Math.floorMod(x, 64)
        val localY = Math.floorMod(y, 64)
        return planeResolver.objectPlane(localX, localY, plane)
    }

    private fun isSkippedObject(x: Int, y: Int, z: Int): Boolean =
        SkippedObjectRepository.key(x, y, z) in skippedObjectKeys

    companion object {
        private val BLOCK_RANGE_FALSE_OVERRIDES = setOf(
            // Bank booths — counters that allow talking/projectiles through
            6083, 6084, 10083, 10355, 10356, 10357,
            10517, 10518, 10527, 10528, 10583, 10584, 10585,
            11338, 12798, 12799, 12800, 12801, 14367, 14368,
            16642, 16643, 16700, 18491, 22819, 25808,
            28564, 28565, 30389, 30390, 30391, 34138,
            // Bank tables (behind the counter)
            590, 591, 2094, 6081, 6082, 15677,
        )

        @JvmField
        val BLOCK_RANGE_FALSE_IDS: Set<Int> = BLOCK_RANGE_FALSE_OVERRIDES

        @JvmStatic
        fun resolveFootprint(normalizedRotation: Int, sizeX: Int, sizeY: Int): Pair<Int, Int> =
            if (normalizedRotation == 1 || normalizedRotation == 3) sizeY to sizeX else sizeX to sizeY

        @JvmStatic
        fun isTypeWalkBlocking(type: Int, solid: Boolean, hasActions: Boolean, name: String? = null): Boolean =
            solid &&
                when {
                    type == 22 -> hasActions
                    type == 10 || type == 11 || type == 9 || type >= 12 -> true
                    type in 0..3 -> true
                    else -> false
                }

        @JvmStatic
        fun isTypeUnwalkable(type: Int, solid: Boolean, walkable: Boolean, hasActions: Boolean): Boolean =
            isTypeWalkBlocking(type, solid, hasActions, null)

        @JvmStatic
        fun occupiesTile(
            objectX: Int,
            objectY: Int,
            tileX: Int,
            tileY: Int,
            type: Int,
            rotation: Int,
            sizeX: Int,
            sizeY: Int,
        ): Boolean {
            val (width, length) = resolveFootprint(rotation and 0x3, sizeX, sizeY)
            return tileX in objectX until (objectX + width) && tileY in objectY until (objectY + length)
        }
    }
}
