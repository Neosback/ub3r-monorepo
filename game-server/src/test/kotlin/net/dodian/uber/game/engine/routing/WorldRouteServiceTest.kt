package net.dodian.uber.game.engine.routing

import net.dodian.uber.game.model.Position
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorldRouteServiceTest {
    @BeforeEach
    fun reset() = WorldRouteService.clear()

    @AfterEach
    fun cleanup() = WorldRouteService.clear()

    @Test
    fun `route reaches an open destination`() {
        WorldRouteService.allocateZone(3200, 3200, 0)
        val route = WorldRouteService.findRoute(0, 3200, 3200, 3206, 3204)
        assertTrue(route.success)
        assertTrue(route.waypoints.isNotEmpty())
    }

    @Test
    fun `blocked destination returns closest alternative but never exact success`() {
        WorldRouteService.allocateZone(3200, 3200, 0)
        WorldRouteService.markTerrainBlocked(3206, 3204, 0)

        val closest = WorldRouteService.findRoute(0, 3200, 3200, 3206, 3204, moveNear = true)
        assertTrue(closest.success)
        assertTrue(closest.alternative)
        assertTrue(closest.waypoints.lastOrNull()?.let { it.x != 3206 || it.z != 3204 } == true)

        val exact = WorldRouteService.findRoute(0, 3200, 3200, 3206, 3204, moveNear = false)
        assertFalse(exact.success)
        assertFalse(exact.alternative)
    }

    @Test
    fun `clicking inside an enclosed building parks outside the blocked footprint`() {
        WorldRouteService.allocateZone(3200, 3210, 0)
        for (x in 3208..3212) {
            for (y in 3208..3212) {
                WorldRouteService.markTerrainBlocked(x, y, 0)
            }
        }

        val route = WorldRouteService.findRoute(0, 3200, 3210, 3210, 3210, moveNear = true)
        assertTrue(route.success)
        assertTrue(route.alternative)
        val parked = route.waypoints.last()
        assertFalse(parked.x in 3208..3212 && parked.z in 3208..3212)
        assertTrue(parked.x in 3198..3222 && parked.z in 3198..3222)
    }

    @Test
    fun `clicking water chooses the nearest legal edge tile`() {
        for (x in 3205..3207) {
            for (y in 3205..3207) {
                WorldRouteService.markTerrainBlocked(x, y, 0)
            }
        }

        val route = WorldRouteService.findRoute(0, 3200, 3206, 3206, 3206, moveNear = true)
        assertTrue(route.success)
        assertTrue(route.alternative)
        val parked = route.waypoints.last()
        assertTrue(parked.x == 3204)
        assertTrue(parked.z == 3206)
    }

    @Test
    fun `symmetric wall updates block then restore travel`() {
        WorldRouteService.allocateZone(3200, 3200, 0)
        val direction = CollisionDirection.EAST
        WorldRouteService.markWall(direction, 0, 3200, 3200, type = 0, impenetrable = true, add = true)
        assertFalse(WorldRouteService.canTravel(0, 3200, 3200, 1, 0))
        assertFalse(WorldRouteService.canTravel(0, 3201, 3200, -1, 0))

        WorldRouteService.markWall(direction, 0, 3200, 3200, type = 0, impenetrable = true, add = false)
        assertTrue(WorldRouteService.canTravel(0, 3200, 3200, 1, 0))
        assertTrue(WorldRouteService.canTravel(0, 3201, 3200, -1, 0))
    }

    @Test
    fun `diagonal corner walls update the matching diagonal neighbor symmetrically`() {
        WorldRouteService.markWall(CollisionDirection.WEST, 0, 3200, 3200, type = 1, impenetrable = true, add = true)
        assertTrue(WorldRouteService.getFlags(3200, 3200, 0) and org.rsmod.routefinder.flag.CollisionFlag.WALL_NORTH_WEST != 0)
        assertTrue(WorldRouteService.getFlags(3199, 3201, 0) and org.rsmod.routefinder.flag.CollisionFlag.WALL_SOUTH_EAST != 0)

        WorldRouteService.markWall(CollisionDirection.WEST, 0, 3200, 3200, type = 1, impenetrable = true, add = false)
        assertFalse(WorldRouteService.getFlags(3200, 3200, 0) and org.rsmod.routefinder.flag.CollisionFlag.WALL_NORTH_WEST != 0)
        assertFalse(WorldRouteService.getFlags(3199, 3201, 0) and org.rsmod.routefinder.flag.CollisionFlag.WALL_SOUTH_EAST != 0)
    }

    @Test
    fun `projectile blocker interrupts line of sight but walk-only wall does not`() {
        WorldRouteService.allocateZone(3200, 3200, 0)
        val source = Position(3200, 3200, 0)
        val target = Position(3203, 3200, 0)
        WorldRouteService.markWall(CollisionDirection.EAST, 0, 3201, 3200, type = 0, impenetrable = true, add = true)
        assertFalse(WorldRouteService.hasLineOfSight(source, target))

        WorldRouteService.clear()
        WorldRouteService.allocateZone(3200, 3200, 0)
        WorldRouteService.markWall(CollisionDirection.EAST, 0, 3201, 3200, type = 0, impenetrable = false, add = true)
        assertTrue(WorldRouteService.hasLineOfSight(source, target))
    }

    @Test
    fun `different planes never share line checks`() {
        assertFalse(WorldRouteService.hasLineOfSight(Position(3200, 3200, 0), Position(3200, 3200, 1)))
        assertFalse(WorldRouteService.hasLineOfWalk(Position(3200, 3200, 0), Position(3200, 3200, 1)))
    }
}
