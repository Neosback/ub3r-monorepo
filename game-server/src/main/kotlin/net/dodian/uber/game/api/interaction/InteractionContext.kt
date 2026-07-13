package net.dodian.uber.game.api.interaction

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

@DslMarker
annotation class GameContentDsl

enum class InteractionOption {
    FIRST, SECOND, THIRD, FOURTH, FIFTH, ATTACK, USE_ITEM, MAGIC
}

sealed interface InteractionContext {
    val player: Client
    val option: InteractionOption
}

data class ObjectInteractionContext(
    override val player: Client,
    override val option: InteractionOption,
    val objectId: Int,
    val position: Position,
    val definition: GameObjectData?,
    val itemPayload: ItemPayload? = null,
    val spellPayload: SpellPayload? = null
) : InteractionContext

data class NpcInteractionContext(
    override val player: Client,
    override val option: InteractionOption,
    val npc: Npc,
    val itemPayload: ItemPayload? = null,
    val spellPayload: SpellPayload? = null
) : InteractionContext

data class ItemPayload(
    val itemId: Int,
    val itemSlot: Int,
    val interfaceId: Int
)

data class SpellPayload(
    val spellId: Int
)
