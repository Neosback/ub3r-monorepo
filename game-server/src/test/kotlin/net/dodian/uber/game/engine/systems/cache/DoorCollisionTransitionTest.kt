package net.dodian.uber.game.engine.systems.cache

import net.dodian.uber.game.engine.systems.pathing.collision.CollisionDirection
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DoorCollisionTransitionTest {
    @Test
    fun `opening a door moves its solid wall from the doorway to the rotated panel`() {
        val collision = CollisionManager()
        val clips = CollisionBuildService(collision)
        val x = 3200
        val y = 3200
        val closedFace = 0 // west-facing wall: blocks west/east passage through the doorway.
        val openFace = 1 // north-facing wall: blocks south/north passage through the open panel.

        applyDoor(clips, x, y, closedFace)
        assertBlocked(collision, x, y, CollisionDirection.WEST)
        assertPassable(collision, x, y, CollisionDirection.NORTH)

        removeDoor(clips, x, y, closedFace)
        applyDoor(clips, x, y, openFace)
        assertPassable(collision, x, y, CollisionDirection.WEST)
        assertBlocked(collision, x, y, CollisionDirection.NORTH)

        removeDoor(clips, x, y, openFace)
        applyDoor(clips, x, y, closedFace)
        assertBlocked(collision, x, y, CollisionDirection.WEST)
        assertPassable(collision, x, y, CollisionDirection.NORTH)
    }

    private fun applyDoor(clips: CollisionBuildService, x: Int, y: Int, face: Int) {
        clips.applyObject(id = 1, x = x, y = y, z = 0, type = 0, rotation = face, sizeX = 1, sizeY = 1, solid = true)
    }

    private fun removeDoor(clips: CollisionBuildService, x: Int, y: Int, face: Int) {
        clips.removeObject(id = 1, x = x, y = y, z = 0, type = 0, rotation = face, sizeX = 1, sizeY = 1, solid = true)
    }

    private fun assertBlocked(collision: CollisionManager, x: Int, y: Int, direction: CollisionDirection) {
        assertFalse(collision.isTraversable(0, x, y, direction, 1))
        assertFalse(collision.isTraversable(0, x + direction.dx, y + direction.dy, direction.opposite(), 1))
    }

    private fun assertPassable(collision: CollisionManager, x: Int, y: Int, direction: CollisionDirection) {
        assertTrue(collision.isTraversable(0, x, y, direction, 1))
        assertTrue(collision.isTraversable(0, x + direction.dx, y + direction.dy, direction.opposite(), 1))
    }
}
