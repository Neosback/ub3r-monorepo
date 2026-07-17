package net.dodian.uber.game.api.content

import net.dodian.uber.game.engine.state.GatheringSessionStateAdapter
import net.dodian.uber.game.engine.state.GroundItemIntentStateAdapter
import net.dodian.uber.game.engine.state.InteractionSessionStateAdapter
import net.dodian.uber.game.engine.state.TeleportIntentStateAdapter
import net.dodian.uber.game.engine.systems.interaction.InteractionIntent
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.GroundItem

object ContentState {
    @JvmStatic
    fun attributes(player: Client): ContentAttributes = object : ContentAttributes {
        override fun <T : Any> get(key: ContentAttributeKey<T>): T? = player.contentRuntimeState.getPluginAttribute(key.id)
        override fun <T : Any> put(key: ContentAttributeKey<T>, value: T) = player.contentRuntimeState.putPluginAttribute(key.id, value)
        override fun remove(key: ContentAttributeKey<*>) = player.contentRuntimeState.removePluginAttribute(key.id)
    }

    @JvmStatic
    fun clearSessionState(player: Client) = player.contentRuntimeState.clearPluginAttributes()
    @JvmStatic
    fun beginGatheringSession(player: Client, sessionKey: String): Boolean = GatheringSessionStateAdapter.begin(player, sessionKey)

    @JvmStatic
    fun endGatheringSession(player: Client, sessionKey: String) = GatheringSessionStateAdapter.end(player, sessionKey)

    @JvmStatic
    fun interruptGatheringSession(player: Client) = GatheringSessionStateAdapter.interrupt(player)

    @JvmStatic
    fun pendingInteraction(player: Client): InteractionIntent? = InteractionSessionStateAdapter.pending(player)

    @JvmStatic
    fun scheduleInteraction(player: Client, intent: InteractionIntent) = InteractionSessionStateAdapter.schedule(player, intent)

    @JvmStatic
    fun clearInteraction(player: Client) = InteractionSessionStateAdapter.clear(player)

    @JvmStatic
    fun beginGroundPickup(player: Client, target: GroundItem) = GroundItemIntentStateAdapter.beginPickup(player, target)

    @JvmStatic
    fun clearGroundPickup(player: Client) = GroundItemIntentStateAdapter.clearPickup(player)

    @JvmStatic
    fun groundPickupTarget(player: Client): GroundItem? = GroundItemIntentStateAdapter.target(player)

    @JvmStatic
    fun groundPickupWanted(player: Client): Boolean = GroundItemIntentStateAdapter.wantsPickup(player)

    @JvmStatic
    fun isTeleportingOrTeleported(player: Client): Boolean = TeleportIntentStateAdapter.isTeleportingOrTeleported(player)
}
