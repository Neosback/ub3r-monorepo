package net.dodian.uber.game.engine.systems.cache

import net.dodian.uber.game.engine.routing.CollisionDirection
import net.dodian.uber.game.engine.routing.WorldRouteService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DoorCollisionTransitionTest {
    @Test
    fun `opening a door moves its solid wall from the doorway to the rotated panel`() {
        WorldRouteService.clear()
        val clips = CollisionBuildService(WorldRouteService)
        val x = 3200
        val y = 3200
        val closedFace = 0 // west-facing wall: blocks west/east passage through the doorway.
        val openFace = 1 // north-facing wall: blocks south/north passage through the open panel.

        applyDoor(clips, x, y, closedFace)
        assertBlocked(x, y, CollisionDirection.WEST)
        assertPassable(x, y, CollisionDirection.NORTH)

        removeDoor(clips, x, y, closedFace)
        applyDoor(clips, x, y, openFace)
        assertPassable(x, y, CollisionDirection.WEST)
        assertBlocked(x, y, CollisionDirection.NORTH)

        removeDoor(clips, x, y, openFace)
        applyDoor(clips, x, y, closedFace)
        assertBlocked(x, y, CollisionDirection.WEST)
        assertPassable(x, y, CollisionDirection.NORTH)
    }

    private fun applyDoor(clips: CollisionBuildService, x: Int, y: Int, face: Int) {
        clips.applyObject(id = 1, x = x, y = y, z = 0, type = 0, rotation = face, sizeX = 1, sizeY = 1, solid = true)
    }

    private fun removeDoor(clips: CollisionBuildService, x: Int, y: Int, face: Int) {
        clips.removeObject(id = 1, x = x, y = y, z = 0, type = 0, rotation = face, sizeX = 1, sizeY = 1, solid = true)
    }

    private fun assertBlocked(x: Int, y: Int, direction: CollisionDirection) {
        assertFalse(WorldRouteService.canTravel(0, x, y, direction.dx, direction.dy))
        assertFalse(WorldRouteService.canTravel(0, x + direction.dx, y + direction.dy, -direction.dx, -direction.dy))
    }

    private fun assertPassable(x: Int, y: Int, direction: CollisionDirection) {
        assertTrue(WorldRouteService.canTravel(0, x, y, direction.dx, direction.dy))
        assertTrue(WorldRouteService.canTravel(0, x + direction.dx, y + direction.dy, -direction.dx, -direction.dy))
    }
}
