package net.dodian.uber.game.engine.systems.event

import java.time.Clock
import java.time.ZonedDateTime
import java.util.concurrent.CopyOnWriteArrayList
import net.dodian.uber.game.events.WorldTickEvent

interface ServerEventSchedule {
    val event: ServerEvent
    fun shouldBeActive(now: ZonedDateTime): Boolean
}

object GameEventScheduleService {
    private val schedules = CopyOnWriteArrayList<ServerEventSchedule>()
    private var clock: Clock = Clock.systemDefaultZone()

    fun onWorldTick(event: WorldTickEvent) {
        if (event.cycle % EVALUATE_EVERY_TICKS != 0) {
            return
        }
        evaluate(ZonedDateTime.now(clock))
    }

    fun evaluate(now: ZonedDateTime = ZonedDateTime.now(clock)) {
        for (schedule in schedules) {
            GameEventService.setScheduledEvent(
                event = schedule.event,
                active = schedule.shouldBeActive(now),
                reason = "Scheduled event window",
            )
        }
    }

    fun register(schedule: ServerEventSchedule) {
        schedules += schedule
    }

    fun clearForTests() {
        schedules.clear()
        clock = Clock.systemDefaultZone()
    }

    fun setClockForTests(value: Clock) {
        clock = value
    }

    private const val EVALUATE_EVERY_TICKS = 100
}
