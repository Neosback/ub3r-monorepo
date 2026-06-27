package net.dodian.uber.game.engine.systems.pathing

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.engine.systems.cache.CollisionBuildService
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionDirection
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionFlag
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionMatrix
import net.dodian.uber.game.engine.systems.pathing.collision.InteractionReachService
import net.dodian.uber.game.engine.systems.pathing.collision.ProjectileLineService
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.objects.WorldObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TarnishTraversalTest {
    @Test
    fun `straight wall flags match Tarnish constants and are removable`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.markWall(CollisionDirection.EAST, 0, 10, 10, 0, impenetrable = true)

        assertEquals(CollisionFlag.WALL_EAST or CollisionFlag.IMPENETRABLE_WALL_EAST, collision.getFlags(10, 10, 0))
        assertEquals(CollisionFlag.WALL_WEST or CollisionFlag.IMPENETRABLE_WALL_WEST, collision.getFlags(11, 10, 0))
        assertFalse(collision.canMove(10, 10, 11, 10, 0, 1, 1))
        assertFalse(collision.canMove(11, 10, 10, 10, 0, 1, 1))

        collision.unmarkWall(CollisionDirection.EAST, 0, 10, 10, 0, impenetrable = true)
        assertEquals(0, collision.getFlags(10, 10, 0))
        assertEquals(0, collision.getFlags(11, 10, 0))
        assertTrue(collision.canMove(10, 10, 11, 10, 0, 1, 1))
    }

    @Test
    fun `entire wall and diagonal corner use Tarnish paired flags`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.markWall(CollisionDirection.NORTH, 0, 20, 20, 2, impenetrable = false)

        assertEquals(CollisionFlag.WALL_EAST or CollisionFlag.WALL_NORTH, collision.getFlags(20, 20, 0))
        assertEquals(CollisionFlag.WALL_SOUTH, collision.getFlags(20, 21, 0))
        assertEquals(CollisionFlag.WALL_WEST, collision.getFlags(21, 20, 0))

        collision.markWall(CollisionDirection.SOUTH, 0, 30, 30, 1, impenetrable = true)
        assertEquals(CollisionFlag.WALL_SOUTH_WEST or CollisionFlag.IMPENETRABLE_WALL_SOUTH_WEST, collision.getFlags(30, 30, 0))
        assertEquals(CollisionFlag.WALL_NORTH_EAST or CollisionFlag.IMPENETRABLE_WALL_NORTH_EAST, collision.getFlags(29, 29, 0))
    }

    @Test
    fun `occupants mark blocked and impenetrable flags separately`() {
        val collision = CollisionManager(CollisionMatrix())
        collision.markOccupant(0, 40, 40, 2, 2, impenetrable = true, add = true)

        for (x in 40..41) {
            for (y in 40..41) {
                assertEquals(CollisionFlag.BLOCKED or CollisionFlag.IMPENETRABLE_BLOCKED, collision.getFlags(x, y, 0))
                assertTrue(collision.isTileBlocked(x, y, 0))
            }
        }

        collision.markOccupant(0, 40, 40, 2, 2, impenetrable = true, add = false)
        for (x in 40..41) for (y in 40..41) assertEquals(0, collision.getFlags(x, y, 0))
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
        builder.applyObject(1, 30, 30, 0, 10, 1, 2, 3, true, impenetrable = true)
        for (x in 30..32) for (y in 30..31) assertTrue(collision.isTileBlocked(x, y, 0))
        builder.removeObject(1, 30, 30, 0, 10, 1, 2, 3, true, impenetrable = true)
        for (x in 30..32) for (y in 30..31) assertFalse(collision.isTileBlocked(x, y, 0))
    }

    @Test
    fun `object reach uses Tarnish rotated footprint and walking flag`() {
        val objectId = 99_001
        GameObjectData.addDefinition(
            GameObjectData(
                id = objectId,
                name = "reach-test",
                description = "reach-test",
                sizeX = 2,
                sizeY = 3,
                solid = true,
                impenetrable = true,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
                walkingFlag = 0x4,
            ),
        )
        val worldObject = WorldObject(objectId, 50, 50, 0, 10, 1)

        assertTrue(InteractionReachService.reachedObject(Position(50, 50, 0), worldObject))
        assertTrue(InteractionReachService.reachedObject(Position(50, 52, 0), worldObject))
        assertFalse(InteractionReachService.reachedObject(Position(49, 50, 0), worldObject))
        assertTrue(InteractionReachService.reachedObject(Position(52, 50, 0), worldObject))
    }
}
