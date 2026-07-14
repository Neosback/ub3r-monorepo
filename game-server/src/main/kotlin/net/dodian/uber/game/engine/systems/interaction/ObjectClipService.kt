package net.dodian.uber.game.engine.systems.interaction

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditObject
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditStore
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.objects.DoorRegistry
import net.dodian.uber.game.model.objects.TomlRemovedObjectLoader
import net.dodian.uber.game.model.objects.WorldObject
import net.dodian.uber.game.engine.systems.cache.CollisionBuildService
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory

object ObjectClipService {
    private val collisionBuildService = CollisionBuildService(CollisionManager.global())
    private val logger = LoggerFactory.getLogger(ObjectClipService::class.java)

    data class AppliedClip(
        val position: Position,
        val objectId: Int,
        val type: Int,
        val direction: Int,
        val solid: Boolean,
    )

    private val appliedClips = ConcurrentHashMap<String, AppliedClip>()

    @JvmStatic
    fun bootstrapStartupOverlays(worldObjects: Iterable<WorldObject>) {
        clearTrackedClips()

        var appliedWorldObjects = 0
        for (worldObject in worldObjects) {
            if (worldObject.id <= 0 || worldObject.x <= 0 || worldObject.y <= 0) {
                continue
            }
            applyDecodedObject(
                position = Position(worldObject.x, worldObject.y, worldObject.z),
                objectId = worldObject.id,
                type = worldObject.type,
                direction = worldObject.face,
                obj = GameObjectData.forId(worldObject.id),
            )
            appliedWorldObjects++
        }

        // Clear cached-object collision at TOML-removed positions before
        // door application, so stale cache data never feeds into door collision.
        applyTomlRemovalsBeforeDoors()

        var appliedDoors = 0
        for (index in DoorRegistry.doorId.indices) {
            val objectId = DoorRegistry.doorId[index]
            val x = DoorRegistry.doorX[index]
            val y = DoorRegistry.doorY[index]
            if (objectId <= 0 || x <= 0 || y <= 0) {
                continue
            }
            val position = Position(x, y, DoorRegistry.doorHeight[index])
            removeCachedObjectsAt(position, type = 0)
            removeCachedObjectsAt(position, type = 9)

            applyDoor(position, objectId, DoorRegistry.doorFace[index])
            appliedDoors++
        }

        logger.info(
            "Applied startup collision overlays: worldObjects={}, doors={}",
            appliedWorldObjects,
            appliedDoors,
        )

        applyStaticOverrides(StaticObjectOverrides.all())
    }

    @JvmStatic
    fun applyTomlRemovalsBeforeDoors() {
        val removals = TomlRemovedObjectLoader.load()
        for (entry in removals) {
            val position = Position(entry.x, entry.y, entry.z)
            removeDecodedObject(position)
            removeCachedObjectsAt(position, entry.type)
        }
        if (removals.isNotEmpty()) {
            logger.info("Applied TOML-based object removals before doors: count={}", removals.size)
        }
    }

    @JvmStatic
    fun applyStaticOverrides(overrides: Iterable<StaticObjectOverride>) {
        var applied = 0
        for (override in overrides) {
            applyStaticOverride(override)
            applied++
        }
        if (applied > 0) {
            logger.info("Applied static map overrides: count={}", applied)
        }
    }

    @JvmStatic
    fun applyDecodedObject(position: Position, objectId: Int, type: Int, direction: Int, obj: GameObjectData?, forceSolid: Boolean = false, solidOverride: Boolean? = null, blockWalkOverride: Int? = null, blockRangeOverride: Boolean? = null) {
        removeDecodedObject(position)
        if (obj == null) {
            return
        }
        val effectiveSolid = solidOverride ?: (forceSolid || obj.isSolid())
        val effectiveBlockWalk = blockWalkOverride ?: if (forceSolid) 2 else obj.blockWalk()
        val effectiveBlockRange = blockRangeOverride ?: if (forceSolid) true else obj.blockRange()
        appliedClips[key(position)] = AppliedClip(position.copy(), objectId, type, direction, effectiveSolid)
        collisionBuildService.applyObject(
            id = objectId,
            x = position.x,
            y = position.y,
            z = position.z,
            type = type,
            rotation = direction,
            sizeX = obj.sizeX,
            sizeY = obj.sizeY,
            solid = effectiveSolid,
            walkable = if (forceSolid) false else obj.isWalkable(),
            hasActions = obj.hasActions(),
            objectName = obj.name,
            blockWalk = effectiveBlockWalk,
            blockRange = effectiveBlockRange,
            breakRouteFinding = obj.breakRouteFinding(),
            impenetrable = if (objectId in CollisionBuildService.BLOCK_RANGE_FALSE_IDS) false else obj.isImpenetrable(),
            decoration = obj.isDecoration(),
        )
    }

    /**
     * Applies a configured door as a solid straight wall at its current face.
     *
     * Opening a door changes its face, rather than removing its collision: the old doorway edge
     * becomes traversable and the rotated door panel blocks its new edge.  Keeping this here makes
     * startup overlays and live door toggles use the exact same collision policy.
     */
    @JvmStatic
    fun applyDoor(position: Position, objectId: Int, face: Int) {
        applyDecodedObject(
            position = position,
            objectId = objectId,
            type = 0,
            direction = face,
            obj = GameObjectData.forId(objectId),
            forceSolid = true,
        )
    }

    @JvmStatic
    fun removeDecodedObject(position: Position) {
        removeTrackedClip(position)
    }

    @Suppress("UNUSED_PARAMETER")
    fun remove(position: Position, type: Int, direction: Int, solid: Boolean) {
        // Removal only updates the applied-clip bookkeeper; collision flags remain managed by the
        // decoded-object path through CollisionBuildService and the global collision manager.
        removeTrackedClip(position)
    }

    internal fun getAppliedForTests(position: Position): AppliedClip? = appliedClips[key(position)]

    @JvmStatic
    fun getAppliedClip(position: Position): AppliedClip? = appliedClips[key(position)]

    internal fun clearForTests() {
        clearTrackedClips()
    }

    private fun clearTrackedClips() {
        appliedClips.values.map { it.position.copy() }.forEach(::removeTrackedClip)
    }

    private fun applyStaticOverride(override: StaticObjectOverride) {
        removeDecodedObject(override.position)
        removeCachedObjectsForStaticOverride(override)
        if (override.replacementObjectId >= 0) {
            applyDecodedObject(
                position = override.position,
                objectId = override.replacementObjectId,
                type = override.replacementType,
                direction = override.replacementFace,
                obj = GameObjectData.forId(override.replacementObjectId),
            )
        }
    }

    private fun removeCachedObjectsAt(position: Position, type: Int?) {
        CacheCollisionAuditStore.objectsForTile(position.x, position.y)
            .asSequence()
            .filter { !it.skipped && it.x == position.x && it.y == position.y && it.plane == position.z && (type == null || it.type == type) }
            .forEach { removeCachedObject(it) }
    }

    private fun removeCachedObjectsForStaticOverride(override: StaticObjectOverride) {
        CacheCollisionAuditStore.objectsForTile(override.position.x, override.position.y)
            .asSequence()
            .filter { it.matchesStaticOverrideRemoval(override) }
            .forEach { removeCachedObject(it) }
    }

    private fun CacheCollisionAuditObject.matchesStaticOverrideRemoval(override: StaticObjectOverride): Boolean {
        if (skipped) {
            return false
        }
        if (x != override.position.x || y != override.position.y || plane != override.position.z) {
            return false
        }
        if (type != override.replacementType) {
            return false
        }
        return override.replacementFace < 0 || rotation == (override.replacementFace and 0x3)
    }

    private fun removeCachedObject(obj: CacheCollisionAuditObject) {
        val definition = GameObjectData.forId(obj.objectId)
        collisionBuildService.removeObject(
            id = obj.objectId,
            x = obj.x,
            y = obj.y,
            z = obj.plane,
            type = obj.type,
            rotation = obj.rotation,
            sizeX = definition.sizeX,
            sizeY = definition.sizeY,
            solid = definition.isSolid(),
            walkable = definition.isWalkable(),
            hasActions = definition.hasActions(),
            objectName = definition.name,
            blockWalk = definition.blockWalk(),
            blockRange = definition.blockRange(),
            breakRouteFinding = definition.breakRouteFinding(),
            impenetrable = definition.isImpenetrable(),
            decoration = definition.isDecoration(),
        )
    }

    private fun removeTrackedClip(position: Position) {
        val existing = appliedClips.remove(key(position)) ?: return
        val definition = GameObjectData.forId(existing.objectId)
        collisionBuildService.removeObject(
            id = existing.objectId,
            x = existing.position.x,
            y = existing.position.y,
            z = existing.position.z,
            type = existing.type,
            rotation = existing.direction,
            sizeX = definition.sizeX,
            sizeY = definition.sizeY,
            solid = existing.solid,
            walkable = !existing.solid,
            hasActions = definition.hasActions(),
            objectName = definition.name,
            blockWalk = if (existing.solid) 2 else 0,
            blockRange = existing.solid,
            breakRouteFinding = definition.breakRouteFinding(),
            impenetrable = if (existing.solid) definition.isImpenetrable() else false,
            decoration = definition.isDecoration(),
        )
    }

    private fun key(position: Position): String = "${position.x}:${position.y}:${position.z}"
}
