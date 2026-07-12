package net.dodian.uber.game.engine.systems.pathing.collision

import net.dodian.uber.game.model.Position
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProjectileLineServiceTest {
    @Test
    fun `open cardinal and diagonal rays are clear`() {
        val collision = CollisionManager()

        assertTrue(ProjectileLineService.hasLineOfSight(Position(0, 0, 0), Position(6, 0, 0), collision))
        assertTrue(ProjectileLineService.hasLineOfSight(Position(0, 0, 0), Position(6, 4, 0), collision))
    }

    @Test
    fun `impenetrable wall blocks a cardinal projectile ray`() {
        val collision = CollisionManager()
        collision.wall(1, 0, 0, CollisionDirection.WEST, impenetrable = true)

        val result = ProjectileLineService.trace(
            ProjectileLineService.Footprint(0, 0, 1, 0),
            ProjectileLineService.Footprint(3, 0, 1, 0),
            collision,
        )

        assertFalse(result.clear)
        assertNotNull(result.blockedAt)
    }

    @Test
    fun `walk-only wall does not block projectile ray`() {
        val collision = CollisionManager()
        collision.wall(1, 0, 0, CollisionDirection.WEST, impenetrable = false)

        assertTrue(ProjectileLineService.hasLineOfSight(Position(0, 0, 0), Position(3, 0, 0), collision))
    }

    @Test
    fun `diagonal ray cannot cut an impenetrable corner`() {
        val collision = CollisionManager()
        collision.flagSolid(1, 0, 0, impenetrable = true)

        assertFalse(ProjectileLineService.hasLineOfSight(Position(0, 0, 0), Position(2, 2, 0), collision))
    }

    @Test
    fun `multi tile target uses an alternate clear target tile`() {
        val collision = CollisionManager()
        collision.flagSolid(3, 0, 0, impenetrable = true)

        assertTrue(
            ProjectileLineService.hasLineOfSight(
                ProjectileLineService.Footprint(0, 1, 1, 0),
                ProjectileLineService.Footprint(3, 0, 2, 0),
                collision,
            ),
        )
    }

    @Test
    fun `different planes never have line of sight`() {
        val collision = CollisionManager()

        assertFalse(ProjectileLineService.hasLineOfSight(Position(0, 0, 0), Position(0, 0, 1), collision))
    }
}
