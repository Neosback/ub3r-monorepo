package net.dodian.utilities

import net.dodian.cache.objects.GameObjectDef
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditStore
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.objects.DoorRegistry

object Geometry {
    @JvmStatic
    fun goodDistance(
        objectX: Int,
        objectY: Int,
        playerX: Int,
        playerY: Int,
        distance: Int,
    ): Boolean {
        val deltaX = objectX - playerX
        val deltaY = objectY - playerY
        val trueDistance = kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toInt()
        return trueDistance <= distance
    }

    @JvmStatic
    fun goodDistanceObject(
        objectX: Int,
        objectY: Int,
        playerX: Int,
        playerY: Int,
        objectXSize: Int,
        objectYSize: Int,
        z: Int,
    ): Position? {
        if (objectXSize < 1 && objectYSize < 1) {
            if (playerX == objectX && playerY == objectY) {
                return Position(objectX, objectY, z)
            }
            return null
        }
        if (objectXSize == 1 && objectYSize == 1) {
            if (goodDistance(playerX, playerY, objectX, objectY, 1)) {
                return Position(objectX, objectY, z)
            }
        }
        val maxObjX = objectX + objectXSize
        val maxObjY = objectY + objectYSize
        val playerPos = Position(playerX, playerY, z)
        for (x in objectX..maxObjX) {
            for (y in objectY..maxObjY) {
                val pos = Position(x, y, z)
                if (goodDistance(pos.x, pos.y, playerPos.x, playerPos.y, 1) && pos.isPerpendicularTo(playerPos)) {
                    return pos
                }
            }
        }
        return null
    }

    @JvmStatic
    fun goodDistanceObject(
        objectX: Int,
        objectY: Int,
        playerX: Int,
        playerY: Int,
        distance: Int,
        z: Int,
    ): Position? {
        if (goodDistance(playerX, playerY, objectX, objectY, distance)) {
            return Position(objectX, objectY, z)
        }
        return null
    }

    @JvmStatic
    fun delta(
        a: Position,
        b: Position,
    ): Position = Position(b.x - a.x, b.y - a.y)

    @JvmStatic
    fun getDistance(
        coordX1: Int,
        coordY1: Int,
        coordX2: Int,
        coordY2: Int,
    ): Int {
        val deltaX = coordX2 - coordX1
        val deltaY = coordY2 - coordY1
        return kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toInt()
    }

    @JvmStatic
    fun getObject(
        objectId: Int,
        x: Int,
        y: Int,
        h: Int,
    ): GameObjectDef? {
        // Doors are runtime-mutated (opened/closed), so they win over the static cache.
        for (i in DoorRegistry.doorId.indices) {
            if (DoorRegistry.doorId[i] == objectId && DoorRegistry.doorX[i] == x && DoorRegistry.doorY[i] == y) {
                return GameObjectDef(objectId, 2, 0, Position(x, y))
            }
        }
        // Source type + rotation from the decoded cache (matches the client world), not the stale
        // MySQL game_object_definitions table. The cache carries the real type and rotation/face,
        // which the interaction reach (InteractionReachService) needs to compute valid faces.
        for (obj in CacheCollisionAuditStore.objectsForTile(x, y)) {
            if (obj.objectId == objectId && obj.x == x && obj.y == y && obj.plane == h) {
                return GameObjectDef(objectId, obj.type, obj.rotation, Position(x, y))
            }
        }
        return null
    }
}