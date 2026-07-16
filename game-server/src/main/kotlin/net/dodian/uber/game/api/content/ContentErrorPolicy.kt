package net.dodian.uber.game.api.content

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.api.plugin.ContentRouteCatalog
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
            logger.warn("Content handler skipped because binding is quarantined scope={} binding={} slot={} name={} pos={} interface={} recent={}", scope, bindingKey, player.slot, player.playerName, player.position, player.activeInterfaceId, player.describeRecentInboundPackets())
            return defaultValue
        }
        return try {
            action()
        } catch (throwable: Throwable) {
            ContentFaultCircuitBreaker.recordFailure(bindingKey, ContentRouteCatalog.moduleForBinding(bindingKey))
            logger.error(
                "Content handler failure scope={} binding={} slot={} name={} dbId={} pos={} interface={} recent={}",
                scope,
                bindingKey,
                player.slot,
                player.playerName,
                player.dbId,
                player.position,
                player.activeInterfaceId,
                player.describeRecentInboundPackets(),
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
            ContentFaultCircuitBreaker.recordFailure(bindingKey, ContentRouteCatalog.moduleForBinding(bindingKey))
            logger.error(
                "Content handler failure scope={} binding={} slot={} name={} dbId={} pos={} interface={} recent={}",
                scope,
                bindingKey,
                player.slot,
                player.playerName,
                player.dbId,
                player.position,
                player.activeInterfaceId,
                player.describeRecentInboundPackets(),
                throwable,
            )
            null
        }
    }
}
