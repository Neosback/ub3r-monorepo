package net.dodian.uber.game.persistence.player

import net.dodian.uber.game.model.player.skills.Skill
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlayerSaveSqlRepositoryTest {
    @Test
    fun `player supplied serialized values are bound rather than interpolated`() {
        val enabled = Skill.values().count { it.isEnabled() }
        val hostile = "O'Reilly; DROP TABLE characters; -- ü"
        val envelope = PlayerSaveEnvelope(
            sequence = 1,
            createdAt = 1_700_000_000_000,
            dbId = 42,
            playerName = "tester",
            reason = PlayerSaveReason.PERIODIC,
            updateProgress = true,
            finalSave = false,
            dirtyMask = PlayerSaveSegment.ALL_MASK,
            saveRevisionAtCapture = 1,
            segments = listOf(
                StatsSegmentSnapshot(1, 3, 99, IntArray(enabled), 10, 1, intArrayOf(), IntArray(6), 0, 0),
                SocialSegmentSnapshot(listOf(123L)),
                SlayerSegmentSnapshot(hostile, hostile, 0),
                FarmingSegmentSnapshot(hostile, listOf(hostile)),
                EffectsSegmentSnapshot(listOf(1, 2)),
                LooksSegmentSnapshot(hostile, hostile, hostile, hostile),
                MetaSegmentSnapshot(0, 0, listOf(NamedCountEntry(hostile, 1)), listOf(NamedCountEntry(hostile, 1)), 0),
            ),
        )

        val statements = PlayerSaveSqlRepository().buildPreparedStatements(envelope)

        assertTrue(statements.all { hostile !in it.sql })
        assertTrue(statements.any { hostile in it.values })
        assertFalse(statements.any { it.sql.contains("'", ignoreCase = false) })
    }

    @Test
    fun `bank placeholders use existing bank column without changing positive entries`() {
        val enabled = Skill.values().count { it.isEnabled() }
        val envelope = PlayerSaveEnvelope(
            sequence = 2,
            createdAt = 1,
            dbId = 7,
            playerName = "banker",
            reason = PlayerSaveReason.PERIODIC,
            updateProgress = false,
            finalSave = false,
            dirtyMask = PlayerSaveSegment.BANK.mask,
            saveRevisionAtCapture = 1,
            segments = listOf(
                StatsSegmentSnapshot(1, 3, 0, IntArray(enabled), 10, 1, intArrayOf(), IntArray(6), 0, 0),
                BankSegmentSnapshot(
                    entries = listOf(
                        ItemSlotEntry(4, 995, 12_345, 0),
                        ItemSlotEntry(9, 4151, 0, 2),
                    ),
                    placeholdersEnabled = true,
                ),
            ),
        )

        val statement = PlayerSaveSqlRepository().buildPreparedStatements(envelope).last()
        assertTrue("4-995-12345 9-4151-0-2 @ph=1" in statement.values)
    }
}
