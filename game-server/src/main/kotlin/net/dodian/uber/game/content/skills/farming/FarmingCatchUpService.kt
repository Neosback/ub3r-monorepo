package net.dodian.uber.game.content.skills.farming

import java.util.concurrent.ConcurrentHashMap
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.systems.world.pulse.GlobalPulseService

object FarmingCatchUpService {
    private const val MAX_CATCH_UP_PULSES = 288
    private const val MAX_QUEUED_PULSES_PER_PLAYER = 512
    private val queuedCatchUp = ConcurrentHashMap<Long, QueuedCatchUp>()

    private data class QueuedCatchUp(
        val client: Client,
        var pendingPulses: Int,
    )

    @JvmStatic
    fun applyLoginCatchUp(client: Client) {
        applyCatchUp(client, System.currentTimeMillis())
    }

    @JvmStatic
    fun applyInteractionCatchUp(client: Client) {
        applyCatchUp(client, System.currentTimeMillis())
    }

    @JvmStatic
    fun applyCatchUp(
        client: Client,
        nowMillis: Long,
    ): Int {
        val state = client.farmingJson
        val lastPulseAt = state.lastGlobalPulseAtMillis
        if (lastPulseAt <= 0L) {
            state.lastGlobalPulseAtMillis = nowMillis
            client.markFarmingDirty()
            return 0
        }

        val elapsed = nowMillis - lastPulseAt
        if (elapsed < GlobalPulseService.FIVE_MINUTE_PULSE_MS) {
            return 0
        }

        val missedPulses = (elapsed / GlobalPulseService.FIVE_MINUTE_PULSE_MS).toInt()
        val pulsesToApply = missedPulses.coerceAtMost(MAX_CATCH_UP_PULSES)
        enqueueCatchUp(client, pulsesToApply)
        state.lastGlobalPulseAtMillis =
            if (missedPulses > MAX_CATCH_UP_PULSES) {
                nowMillis
            } else {
                lastPulseAt + (pulsesToApply * GlobalPulseService.FIVE_MINUTE_PULSE_MS)
            }
        client.markFarmingDirty()
        return pulsesToApply
    }

    @JvmStatic
    fun drainQueuedCatchUp(maxPulses: Int): Int {
        if (maxPulses <= 0 || queuedCatchUp.isEmpty()) {
            return 0
        }

        var budget = maxPulses
        var applied = 0
        for ((key, queued) in queuedCatchUp) {
            if (budget <= 0) {
                break
            }

            val client = queued.client
            if (!client.isActive || client.disconnected) {
                queuedCatchUp.remove(key, queued)
                continue
            }

            val runNow = minOf(queued.pendingPulses, budget)
            repeat(runNow) {
                client.farming.run { client.updateFarming() }
            }

            applied += runNow
            budget -= runNow
            queued.pendingPulses -= runNow
            if (queued.pendingPulses <= 0) {
                queuedCatchUp.remove(key, queued)
            }
        }

        return applied
    }

    private fun enqueueCatchUp(client: Client, pulsesToApply: Int) {
        if (pulsesToApply <= 0) {
            return
        }
        val key = playerKey(client)
        queuedCatchUp.compute(key) { _, current ->
            if (current == null) {
                QueuedCatchUp(client, pulsesToApply.coerceAtMost(MAX_QUEUED_PULSES_PER_PLAYER))
            } else {
                current.pendingPulses =
                    (current.pendingPulses + pulsesToApply).coerceAtMost(MAX_QUEUED_PULSES_PER_PLAYER)
                current
            }
        }
    }

    private fun playerKey(client: Client): Long =
        when {
            client.dbId > 0 -> (client.dbId.toLong() shl 1)
            client.slot >= 0 -> (client.slot.toLong() shl 1) or 1L
            else -> (System.identityHashCode(client).toLong() shl 2) or 3L
        }
}
