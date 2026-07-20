package net.dodian.uber.game.combat

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AncientSpellRegistryTest {
    @Test
    fun `flat arrays match the original hardcoded Client-java data`() {
        assertArrayEquals(
            intArrayOf(1, 10, 20, 30, 40, 50, 60, 70, 74, 76, 80, 82, 86, 88, 92, 94),
            AncientSpellRegistry.requiredLevel(),
        )
        assertArrayEquals(
            intArrayOf(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32),
            AncientSpellRegistry.baseDamage(),
        )
        assertArrayEquals(
            arrayOf(
                "Smoke Rush", "Shadow Rush", "Blood Rush", "Ice Rush",
                "Smoke Burst", "Shadow Burst", "Blood Burst", "Ice Burst",
                "Smoke Blitz", "Shadow Blitz", "Blood Blitz", "Ice Blitz",
                "Smoke Barrage", "Shadow Barrage", "Blood Barrage", "Ice Barrage",
            ),
            AncientSpellRegistry.spellName(),
        )
        assertArrayEquals(
            intArrayOf(
                12939, 12987, 12901, 12861, 12963, 13011, 12919, 12881,
                12951, 12999, 12911, 12871, 12975, 13023, 12929, 12891,
            ),
            AncientSpellRegistry.ancientId(),
        )
        assertArrayEquals(
            intArrayOf(
                51133, 51185, 51091, 24018, 51159, 51211, 51111, 51069,
                51146, 51198, 51102, 51058, 51172, 51224, 51122, 51080,
            ),
            AncientSpellRegistry.ancientButton(),
        )
        assertArrayEquals(intArrayOf(5, 5, 6, 6), AncientSpellRegistry.coolDown())
    }

    @Test
    fun `gfx lookup matches the original ANCIENT_SPELLS_GFX map`() {
        val smokeRush = AncientSpellRegistry.gfx(0)!!
        assertNull(smokeRush.castGfx)
        assertEquals("smoke_rush_travel", smokeRush.projectileGfx)
        assertEquals("smoke_rush_impact", smokeRush.impactGfx)
        assertEquals(1979, smokeRush.castAnim)

        val shadowBurst = AncientSpellRegistry.gfx(5)!!
        assertNull(shadowBurst.projectileGfx)
        assertEquals("shadow_burst_impact", shadowBurst.impactGfx)

        val bloodBlitz = AncientSpellRegistry.gfx(10)!!
        assertEquals("blood_blitz_travel", bloodBlitz.castGfx)
        assertEquals("blood_blitz_travel", bloodBlitz.projectileGfx)
        assertEquals("blood_blitz_impact", bloodBlitz.impactGfx)
        assertEquals(1978, bloodBlitz.castAnim)

        val iceBarrage = AncientSpellRegistry.gfx(15)!!
        assertEquals("ice_burst_travel", iceBarrage.castGfx)
        assertNull(iceBarrage.projectileGfx)
        assertEquals("ice_barrage_impact", iceBarrage.impactGfx)

        assertNull(AncientSpellRegistry.gfx(16))
    }
}
