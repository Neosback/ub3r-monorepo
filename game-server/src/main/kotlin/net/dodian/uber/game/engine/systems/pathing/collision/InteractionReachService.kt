package net.dodian.uber.game.engine.systems.pathing.collision

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.objects.WorldObject

object InteractionReachService {
    fun reachedObject(start: Position, worldObject: WorldObject): Boolean {
        val definition = GameObjectData.forId(worldObject.id)
        var sizeX = definition.sizeX
        var sizeY = definition.sizeY
        val rotation = worldObject.face and 0x3
        if (rotation % 2 != 0) {
            sizeX = definition.sizeY
            sizeY = definition.sizeX
        }

        var walkingFlag = definition.walkingFlag
        if (rotation != 0) {
            walkingFlag = ((walkingFlag shl rotation) and 0xF) + (walkingFlag shr (4 - rotation))
        }

        if (sizeX == 0 || sizeY == 0 || start.z != worldObject.z) {
            return false
        }

        val destX = worldObject.x
        val destY = worldObject.y
        val cornerX = destX + sizeX - 1
        val cornerY = destY + sizeY - 1
        val x = start.x
        val y = start.y

        if (x in destX..cornerX && y in destY..cornerY) {
            return true
        }

        val flag = CollisionManager.global().getFlags(destX, destY, worldObject.z)
        if (x == destX - 1 && y in destY..cornerY && (CollisionFlag.WALL_EAST and flag) == 0 && (0x8 and walkingFlag) == 0) {
            return true
        }
        if (x == cornerX + 1 && y in destY..cornerY && (CollisionFlag.WALL_WEST and flag) == 0 && (0x2 and walkingFlag) == 0) {
            return true
        }
        if (y == destY - 1 && x in destX..cornerX && (CollisionFlag.WALL_NORTH and flag) == 0 && (0x4 and walkingFlag) == 0) {
            return true
        }
        if (y == cornerY + 1 && x in destX..cornerX && (CollisionFlag.WALL_SOUTH and flag) == 0 && (0x1 and walkingFlag) == 0) {
            return true
        }

        return false
    }
}
