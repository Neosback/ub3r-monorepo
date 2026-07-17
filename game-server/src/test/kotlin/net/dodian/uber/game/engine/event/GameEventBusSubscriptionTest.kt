package net.dodian.uber.game.engine.event

import net.dodian.uber.game.events.GameEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameEventBusSubscriptionTest {
    private data class TestEvent(val value: Int) : GameEvent

    @AfterEach fun cleanUp() = GameEventBus.clear()

    @Test
    fun `owned notification subscription is removable`() {
        val values = mutableListOf<Int>()
        val subscription = GameEventBus.subscribe(TestEvent::class.java, "plugin.test") { values += it.value }
        GameEventBus.post(TestEvent(1))
        subscription.close()
        GameEventBus.post(TestEvent(2))
        assertEquals(listOf(1), values)
    }
}
