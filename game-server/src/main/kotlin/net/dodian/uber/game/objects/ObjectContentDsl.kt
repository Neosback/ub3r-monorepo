package net.dodian.uber.game.objects

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.api.content.ContentObjectInteractionPolicy

import net.dodian.uber.game.api.interaction.GameContentDsl
import net.dodian.uber.game.api.interaction.ObjectInteractionContext
import net.dodian.uber.game.api.interaction.InteractionOption
import net.dodian.uber.game.api.interaction.ItemPayload
import net.dodian.uber.game.api.interaction.SpellPayload

@GameContentDsl
class ObjectContentBuilder {
    private val bindings = mutableListOf<ObjectBinding>()
    private var onFirstClick: (Client, Int, Position, GameObjectData?) -> Boolean = { _, _, _, _ -> false }
    private var onSecondClick: (Client, Int, Position, GameObjectData?) -> Boolean = { _, _, _, _ -> false }
    private var onThirdClick: (Client, Int, Position, GameObjectData?) -> Boolean = { _, _, _, _ -> false }
    private var onFourthClick: (Client, Int, Position, GameObjectData?) -> Boolean = { _, _, _, _ -> false }
    private var onFifthClick: (Client, Int, Position, GameObjectData?) -> Boolean = { _, _, _, _ -> false }
    private var onUseItem:
        (Client, Int, Position, GameObjectData?, Int, Int, Int) -> Boolean =
        { _, _, _, _, _, _, _ -> false }
    private var onMagic:
        (Client, Int, Position, GameObjectData?, Int) -> Boolean =
        { _, _, _, _, _ -> false }

    private var onFirstClickCtx: ((ObjectInteractionContext) -> Boolean)? = null
    private var onSecondClickCtx: ((ObjectInteractionContext) -> Boolean)? = null
    private var onThirdClickCtx: ((ObjectInteractionContext) -> Boolean)? = null
    private var onFourthClickCtx: ((ObjectInteractionContext) -> Boolean)? = null
    private var onFifthClickCtx: ((ObjectInteractionContext) -> Boolean)? = null
    private var onUseItemCtx: ((ObjectInteractionContext) -> Boolean)? = null
    private var onMagicCtx: ((ObjectInteractionContext) -> Boolean)? = null

    private var clickPolicy: (Int, Int, Position, GameObjectData?) -> ContentObjectInteractionPolicy? = { _, _, _, _ -> null }

    fun bind(vararg objectIds: Int) {
        objectIds.sorted().forEach { objectId -> bindings += ObjectBinding(objectId = objectId) }
    }

    fun onFirstClick(handler: (Client, Int, Position, GameObjectData?) -> Boolean) {
        onFirstClick = handler
    }

    fun onFirstClick(handler: (ObjectInteractionContext) -> Boolean) {
        onFirstClickCtx = handler
    }

    fun onSecondClick(handler: (Client, Int, Position, GameObjectData?) -> Boolean) {
        onSecondClick = handler
    }

    fun onSecondClick(handler: (ObjectInteractionContext) -> Boolean) {
        onSecondClickCtx = handler
    }

    fun onThirdClick(handler: (Client, Int, Position, GameObjectData?) -> Boolean) {
        onThirdClick = handler
    }

    fun onThirdClick(handler: (ObjectInteractionContext) -> Boolean) {
        onThirdClickCtx = handler
    }

    fun onFourthClick(handler: (Client, Int, Position, GameObjectData?) -> Boolean) {
        onFourthClick = handler
    }

    fun onFourthClick(handler: (ObjectInteractionContext) -> Boolean) {
        onFourthClickCtx = handler
    }

    fun onFifthClick(handler: (Client, Int, Position, GameObjectData?) -> Boolean) {
        onFifthClick = handler
    }

    fun onFifthClick(handler: (ObjectInteractionContext) -> Boolean) {
        onFifthClickCtx = handler
    }

    fun onUseItem(handler: (Client, Int, Position, GameObjectData?, Int, Int, Int) -> Boolean) {
        onUseItem = handler
    }

    fun onUseItem(handler: (ObjectInteractionContext) -> Boolean) {
        onUseItemCtx = handler
    }

    fun onMagic(handler: (Client, Int, Position, GameObjectData?, Int) -> Boolean) {
        onMagic = handler
    }

    fun onMagic(handler: (ObjectInteractionContext) -> Boolean) {
        onMagicCtx = handler
    }

    fun clickInteractionPolicy(handler: (Int, Int, Position, GameObjectData?) -> ContentObjectInteractionPolicy?) {
        clickPolicy = handler
    }

    fun build(): ObjectContent {
        val resolvedBindings = bindings.toList()
        return object : ObjectContent {
            override fun bindings(): List<ObjectBinding> = resolvedBindings

            override fun onFirstClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean =
                onFirstClickCtx?.invoke(ObjectInteractionContext(client, InteractionOption.FIRST, objectId, position, obj))
                    ?: onFirstClick(client, objectId, position, obj)

            override fun onSecondClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean =
                onSecondClickCtx?.invoke(ObjectInteractionContext(client, InteractionOption.SECOND, objectId, position, obj))
                    ?: onSecondClick(client, objectId, position, obj)

            override fun onThirdClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean =
                onThirdClickCtx?.invoke(ObjectInteractionContext(client, InteractionOption.THIRD, objectId, position, obj))
                    ?: onThirdClick(client, objectId, position, obj)

            override fun onFourthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean =
                onFourthClickCtx?.invoke(ObjectInteractionContext(client, InteractionOption.FOURTH, objectId, position, obj))
                    ?: onFourthClick(client, objectId, position, obj)

            override fun onFifthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean =
                onFifthClickCtx?.invoke(ObjectInteractionContext(client, InteractionOption.FIFTH, objectId, position, obj))
                    ?: onFifthClick(client, objectId, position, obj)

            override fun onUseItem(
                client: Client,
                objectId: Int,
                position: Position,
                obj: GameObjectData?,
                itemId: Int,
                itemSlot: Int,
                interfaceId: Int,
            ): Boolean = onUseItemCtx?.invoke(
                ObjectInteractionContext(
                    player = client,
                    option = InteractionOption.USE_ITEM,
                    objectId = objectId,
                    position = position,
                    definition = obj,
                    itemPayload = ItemPayload(itemId, itemSlot, interfaceId)
                )
            ) ?: onUseItem(client, objectId, position, obj, itemId, itemSlot, interfaceId)

            override fun onMagic(
                client: Client,
                objectId: Int,
                position: Position,
                obj: GameObjectData?,
                spellId: Int,
            ): Boolean = onMagicCtx?.invoke(
                ObjectInteractionContext(
                    player = client,
                    option = InteractionOption.MAGIC,
                    objectId = objectId,
                    position = position,
                    definition = obj,
                    spellPayload = SpellPayload(spellId)
                )
            ) ?: onMagic(client, objectId, position, obj, spellId)

            override fun onFirstClick(context: ObjectInteractionContext): Boolean =
                onFirstClickCtx?.invoke(context) ?: super.onFirstClick(context)

            override fun onSecondClick(context: ObjectInteractionContext): Boolean =
                onSecondClickCtx?.invoke(context) ?: super.onSecondClick(context)

            override fun onThirdClick(context: ObjectInteractionContext): Boolean =
                onThirdClickCtx?.invoke(context) ?: super.onThirdClick(context)

            override fun onFourthClick(context: ObjectInteractionContext): Boolean =
                onFourthClickCtx?.invoke(context) ?: super.onFourthClick(context)

            override fun onFifthClick(context: ObjectInteractionContext): Boolean =
                onFifthClickCtx?.invoke(context) ?: super.onFifthClick(context)

            override fun onUseItem(context: ObjectInteractionContext): Boolean =
                onUseItemCtx?.invoke(context) ?: super.onUseItem(context)

            override fun onMagic(context: ObjectInteractionContext): Boolean =
                onMagicCtx?.invoke(context) ?: super.onMagic(context)

            override fun clickInteractionPolicy(
                option: Int,
                objectId: Int,
                position: Position,
                obj: GameObjectData?,
            ): ContentObjectInteractionPolicy? = clickPolicy(option, objectId, position, obj)
        }
    }
}

fun objectContent(init: ObjectContentBuilder.() -> Unit): ObjectContent {
    return ObjectContentBuilder().apply(init).build()
}

