package net.dodian.uber.game.api.content

import net.dodian.uber.game.model.entity.player.Client
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ContentErrorPolicy {
    private val logger: Logger = LoggerFactory.getLogger(ContentErrorPolicy::class.java)

    @JvmStatic
    fun runBoolean(
        player: Client,
        scope: String,
        bindingKey: String = scope,
        defaultValue: Boolean = false,
        action: () -> Boolean,
    ): Boolean {
        if (!ContentFaultCircuitBreaker.allows(bindingKey)) {
            logger.warn("Content handler skipped because binding is quarantined scope={} binding={} slot={} name={}", scope, bindingKey, player.slot, player.playerName)
            return defaultValue
        }
        return try {
            action()
        } catch (throwable: Throwable) {
            ContentFaultCircuitBreaker.recordFailure(bindingKey)
            logger.error(
                "Content handler failure scope={} slot={} name={} pos={}",
                scope,
                player.slot,
                player.playerName,
                player.position,
                throwable,
            )
            defaultValue
        }
    }

    @JvmStatic
    fun <T> runNullable(
        player: Client,
        scope: String,
        bindingKey: String = scope,
        action: () -> T?,
    ): T? {
        if (!ContentFaultCircuitBreaker.allows(bindingKey)) {
            return null
        }
        return try {
            action()
        } catch (throwable: Throwable) {
            ContentFaultCircuitBreaker.recordFailure(bindingKey)
            logger.error(
                "Content handler failure scope={} slot={} name={} pos={}",
                scope,
                player.slot,
                player.playerName,
                player.position,
                throwable,
            )
            null
        }
    }
}
