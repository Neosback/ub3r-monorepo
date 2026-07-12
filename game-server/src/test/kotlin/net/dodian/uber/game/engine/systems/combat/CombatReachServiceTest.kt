package net.dodian.uber.game.engine.systems.combat

import net.dodian.uber.game.model.EntityType
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.Entity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CombatReachServiceTest {
    @Test
    fun `reports ready when target is in range`() {
        assertEquals(
            CombatReachResult.READY,
            CombatReachService.evaluate(TestEntity(0, 0, 0), TestEntity(5, 0, 0), 5, projectile = false),
        )
    }

    @Test
    fun `reports overlap before range`() {
        assertEquals(
            CombatReachResult.OVERLAPPING,
            CombatReachService.evaluate(TestEntity(10, 10, 0), TestEntity(10, 10, 0), 5, projectile = true),
        )
    }

    @Test
    fun `reports out of range`() {
        assertEquals(
            CombatReachResult.OUT_OF_RANGE,
            CombatReachService.evaluate(TestEntity(0, 0, 0), TestEntity(7, 0, 0), 5, projectile = false),
        )
    }

    @Test
    fun `reports wrong plane`() {
        assertEquals(
            CombatReachResult.WRONG_PLANE,
            CombatReachService.evaluate(TestEntity(0, 0, 0), TestEntity(0, 1, 1), 5, projectile = false),
        )
    }

    private class TestEntity(x: Int, y: Int, z: Int) : Entity(Position(x, y, z), 0, Type.PLAYER) {
        override fun didMove(): Boolean = false
        override fun getEntityType(): EntityType = EntityType.PLAYER
    }
}
