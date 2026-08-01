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

    fun onFirstClick(handler: (ObjectInteractionContext) -> Boolean) {
        onFirstClickCtx = handler
    }

    fun onSecondClick(handler: (ObjectInteractionContext) -> Boolean) {
        onSecondClickCtx = handler
    }

    fun onThirdClick(handler: (ObjectInteractionContext) -> Boolean) {
        onThirdClickCtx = handler
    }

    fun onFourthClick(handler: (ObjectInteractionContext) -> Boolean) {
        onFourthClickCtx = handler
    }

    fun onFifthClick(handler: (ObjectInteractionContext) -> Boolean) {
        onFifthClickCtx = handler
    }

    fun onUseItem(handler: (ObjectInteractionContext) -> Boolean) {
        onUseItemCtx = handler
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

