package net.dodian.uber.game.discord

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiscordConfigurationTest {
    @Test
    fun `discord remains optional without a token`() {
        val configuration = DiscordRuntimeConfiguration("", "1", "2", "3")

        assertFalse(configuration.enabled())
        assertFalse(configuration.valid())
    }

    @Test
    fun `enabled discord requires numeric guild and channel ids`() {
        assertTrue(DiscordRuntimeConfiguration("token", "1", "2", "3").valid())
        assertFalse(DiscordRuntimeConfiguration("token", "guild", "2", "3").valid())
        assertFalse(DiscordRuntimeConfiguration("token", "1", "", "3").valid())
        assertFalse(DiscordRuntimeConfiguration("token", "1", "2", "alerts").valid())
    }

    @Test
    fun `only mutation-adjacent slash commands require administrator permission`() {
        assertFalse(DiscordCommandPolicy.requiresAdministrator("players"))
        assertFalse(DiscordCommandPolicy.requiresAdministrator("status"))
        assertTrue(DiscordCommandPolicy.requiresAdministrator("announce"))
        assertTrue(DiscordCommandPolicy.requiresAdministrator("alert-test"))
        assertFalse(DiscordCommandPolicy.isAllowed("announce", false))
        assertTrue(DiscordCommandPolicy.isAllowed("announce", true))
    }
}
