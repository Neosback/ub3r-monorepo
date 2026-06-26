package net.dodian.uber.game.engine.systems.pathing

import net.dodian.uber.game.engine.systems.pathing.collision.CollisionDirection
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionMatrix
import net.dodian.uber.game.engine.systems.pathing.collision.ProjectileLineService
import net.dodian.uber.game.engine.systems.cache.CollisionBuildService
import net.dodian.uber.game.model.Position
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class TarnishTraversalTest {
    @Test
    fun `wall flags are symmetric and removable`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.wall(10, 10, 0, CollisionDirection.EAST)
        assertFalse(collision.canMove(10, 10, 11, 10, 0, 1, 1))
        assertFalse(collision.canMove(11, 10, 10, 10, 0, 1, 1))
        collision.clearWall(10, 10, 0, CollisionDirection.EAST)
        assertTrue(collision.canMove(10, 10, 11, 10, 0, 1, 1))
    }

    @Test
    fun `diagonal cannot cut a blocked corner`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.flagSolid(11, 10, 0)
        collision.flagSolid(10, 11, 0)
        assertFalse(collision.canMove(10, 10, 11, 11, 0, 1, 1))
    }

    @Test
    fun `dijkstra detours and entity size is respected`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.flagSolid(11, 10, 0)
        val algorithm = DijkstraPathfindingAlgorithm { x, y, z, dx, dy -> collision.traversable(x, y, z, dx, dy) }
        val path = algorithm.find(10, 10, 12, 10, 0)
        assertTrue(path.isNotEmpty())
        assertEquals(12, path.last.x)
        assertTrue(collision.canMove(20, 20, 21, 20, 0, 2, 2))
        collision.flagSolid(22, 21, 0)
        assertFalse(collision.canMove(20, 20, 21, 20, 0, 2, 2))
    }

    @Test
    fun `dijkstra fallback finds closest reachable tile`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.flagSolid(12, 10, 0)
        val algorithm = DijkstraPathfindingAlgorithm { x, y, z, dx, dy -> collision.traversable(x, y, z, dx, dy) }
        val path = algorithm.find(10, 10, 12, 10, 0)
        assertTrue(path.isNotEmpty())
        assertEquals(11, path.last.x)
        assertEquals(10, path.last.y)
    }


    @Test
    fun `projectiles respect impenetrable clipping`() {
        val collision = CollisionManager(CollisionMatrix())
        assertTrue(ProjectileLineService.hasLineOfSight(Position(10, 10, 0), Position(13, 10, 0), collision))
        collision.flagSolid(12, 10, 0, impenetrable = true)
        assertFalse(ProjectileLineService.hasLineOfSight(Position(10, 10, 0), Position(13, 10, 0), collision))
    }

    @Test
    fun `rotated multi tile object mark and unmark are symmetric`() {
        val collision = CollisionManager(CollisionMatrix())
        val builder = CollisionBuildService(collision)
        builder.applyObject(1, 30, 30, 0, 10, 1, 2, 3, true, false, true, "test", 2, true, false)
        for (x in 30..32) for (y in 30..31) assertTrue(collision.isTileBlocked(x, y, 0))
        builder.removeObject(1, 30, 30, 0, 10, 1, 2, 3, true, false, true, "test", 2, true, false)
        for (x in 30..32) for (y in 30..31) assertFalse(collision.isTileBlocked(x, y, 0))
    }
}