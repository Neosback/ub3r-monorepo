package net.dodian.uber.game.model.entity.player

import net.dodian.uber.game.engine.loop.GameThreadContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InterfaceManagerTest {

    @BeforeEach
    fun setUp() {
        GameThreadContext.bindCurrentThread()
    }

    @AfterEach
    fun tearDown() {
        GameThreadContext.clearBindingForTests()
    }

    @Test
    fun `opening interface tracks open interface id`() {
        val client = Client(null, 1).apply { playerName = "test-player" }
        assertFalse(client.interfaceManager.isInterfaceOpen(1234))
        assertEquals(-1, client.interfaceManager.main)

        client.interfaceManager.open(1234, false) // open with false to bypass combat/null checks

        assertTrue(client.interfaceManager.isInterfaceOpen(1234))
        assertEquals(1234, client.interfaceManager.main)
    }

    @Test
    fun `closing interfaces clears open interface id and resets state`() {
        val client = Client(null, 1).apply { playerName = "test-player" }
        client.interfaceManager.open(1234, false)
        client.IsBanking = true

        client.interfaceManager.close()

        assertFalse(client.interfaceManager.isInterfaceOpen(1234))
        assertEquals(-1, client.interfaceManager.main)
        assertFalse(client.IsBanking)
    }

    @Test
    fun `setSidebar updates sidebar interfaces`() {
        val client = Client(null, 1).apply { playerName = "test-player" }
        client.interfaceManager.setSidebar(3, 3213)
        assertEquals(3213, client.interfaceManager.getSidebar(3))
        assertTrue(client.interfaceManager.isSidebar(3, 3213))
    }
}
