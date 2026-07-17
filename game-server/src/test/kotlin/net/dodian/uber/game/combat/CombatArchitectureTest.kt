package net.dodian.uber.game.combat

import net.dodian.uber.game.model.entity.Entity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CombatArchitectureTest {
    @Test
    fun `legacy styles resolve through strategy contracts`() {
        assertSame(MeleeCombatStrategy, PlayerCombatStrategies.resolve(0))
        assertSame(RangedCombatStrategy, PlayerCombatStrategies.resolve(1))
        assertSame(MagicCombatStrategy, PlayerCombatStrategies.resolve(2))
        assertNull(PlayerCombatStrategies.resolve(3))
    }

    @Test
    fun `strategy profiles declare reach projectile and damage behavior`() {
        assertEquals(1, MeleeCombatStrategy.profile.reach)
        assertFalse(MeleeCombatStrategy.profile.projectile)
        assertEquals(Entity.damageType.MELEE, MeleeCombatStrategy.profile.damageType)

        assertEquals(5, RangedCombatStrategy.profile.reach)
        assertTrue(RangedCombatStrategy.profile.projectile)
        assertEquals(Entity.damageType.RANGED, RangedCombatStrategy.profile.damageType)

        assertEquals(5, MagicCombatStrategy.profile.reach)
        assertTrue(MagicCombatStrategy.profile.projectile)
        assertEquals(Entity.damageType.MAGIC, MagicCombatStrategy.profile.damageType)
    }
}
