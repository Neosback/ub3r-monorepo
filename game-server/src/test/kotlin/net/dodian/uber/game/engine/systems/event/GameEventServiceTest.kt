package net.dodian.uber.game.engine.systems.event

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GameEventServiceTest {
    @AfterEach
    fun tearDown() {
        GameEventService.clearForTests()
        GameEventScheduleService.clearForTests()
    }

    @Test
    fun `admin override wins over scheduler state`() {
        GameEventService.startEvent(ServerEvent.CHRISTMAS, EventSource.ADMIN_COMMAND, "manual test")
        GameEventService.setScheduledEvent(ServerEvent.CHRISTMAS, active = false)

        assertTrue(GameEventService.isActive(ServerEvent.CHRISTMAS))
    }

    @Test
    fun `scheduler can start and clear scheduler owned event`() {
        GameEventScheduleService.register(
            object : ServerEventSchedule {
                override val event = ServerEvent.EASTER
                override fun shouldBeActive(now: ZonedDateTime): Boolean = now.toLocalDate() == LocalDate.of(2026, 4, 5)
            }
        )

        GameEventScheduleService.evaluate(ZonedDateTime.of(2026, 4, 5, 12, 0, 0, 0, ZoneOffset.UTC))
        assertTrue(GameEventService.isActive(ServerEvent.EASTER))

        GameEventScheduleService.evaluate(ZonedDateTime.of(2026, 4, 6, 12, 0, 0, 0, ZoneOffset.UTC))
        assertFalse(GameEventService.isActive(ServerEvent.EASTER))
    }
}
