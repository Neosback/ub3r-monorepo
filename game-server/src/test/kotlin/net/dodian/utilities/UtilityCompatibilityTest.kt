package net.dodian.utilities

import net.dodian.uber.game.model.player.skills.Skills
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class UtilityCompatibilityTest {
    @Test
    fun `xp table matches legacy formula at levels and boundaries`() {
        for (level in -10..120) {
            assertEquals(legacyXpForLevel(level), Skills.getXPForLevel(level), "level=$level")
        }

        val experiences = buildList {
            add(-1)
            add(0)
            add(Int.MAX_VALUE)
            for (level in 1..99) {
                val threshold = legacyXpForLevel(level)
                add(threshold - 1)
                add(threshold)
                add(threshold + 1)
            }
        }
        for (experience in experiences) {
            assertEquals(legacyLevelForExperience(experience), Skills.getLevelForExperience(experience), "xp=$experience")
        }
    }

    @Test
    fun `text unpack uses independent buffers for concurrent messages`() {
        val eMessage = ByteArray(1024) { 0x11 }
        val tMessage = ByteArray(1024) { 0x22 }
        val expectedE = "e".repeat(2048)
        val expectedT = "t".repeat(2048)
        val executor = Executors.newFixedThreadPool(8)
        try {
            val work = (0 until 8).map { worker ->
                Callable {
                    repeat(250) {
                        val actual = if (worker % 2 == 0) TextCodec.textUnpack(eMessage, eMessage.size) else TextCodec.textUnpack(tMessage, tMessage.size)
                        assertEquals(if (worker % 2 == 0) expectedE else expectedT, actual)
                    }
                }
            }
            executor.invokeAll(work).forEach { it.get() }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `legacy md5 and salted password hash remain stable`() {
        val firstHash = MD5("password").compute()

        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", firstHash)
        assertEquals("d514dee5e76bbb718084294c835f312c", MD5(firstHash + "salt").compute())
    }

    private fun legacyLevelForExperience(experience: Int): Int {
        val safeExperience = experience.coerceAtLeast(0)
        var points = 0.0
        for (level in 1..99) {
            points += kotlin.math.floor(level + 300.0 * Math.pow(2.0, level.toDouble() / 7.0))
            if (safeExperience < kotlin.math.floor(points / 4).toInt()) {
                return level
            }
        }
        return 99
    }

    private fun legacyXpForLevel(level: Int): Int {
        var points = 0.0
        var output = 0
        for (currentLevel in 1 until level) {
            points += kotlin.math.floor(currentLevel + 300.0 * Math.pow(2.0, currentLevel.toDouble() / 7.0))
            output = kotlin.math.floor(points / 4).toInt()
        }
        return output
    }
}
