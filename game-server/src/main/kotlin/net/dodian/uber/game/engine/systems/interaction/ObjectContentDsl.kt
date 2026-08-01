package net.dodian.uber.game.engine.systems.interaction

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.api.interaction.ObjectInteractionContext

data class FirstClickObjectAction(
    val objectIds: IntArray,
    val handler: (Client, Int, Position, GameObjectData?) -> Boolean,
)

class FirstClickObjectActionBuilder internal constructor() {
    private val actions = ArrayList<FirstClickObjectAction>()

    fun objectAction(vararg objectIds: Int, handler: (Client, Int, Position, GameObjectData?) -> Boolean) {
        require(objectIds.isNotEmpty()) { "objectAction requires at least one object id" }
        actions += FirstClickObjectAction(objectIds = objectIds.copyOf(), handler = handler)
    }

    internal fun build(): List<FirstClickObjectAction> = actions.toList()
}

fun firstClickObjectActions(block: FirstClickObjectActionBuilder.() -> Unit): List<FirstClickObjectAction> {
    val builder = FirstClickObjectActionBuilder()
    builder.block()
    return builder.build()
}

abstract class FirstClickDslObjectContent(
    actions: List<FirstClickObjectAction>,
) : ObjectContent {
    private val actionsByObjectId: Map<Int, List<(Client, Int, Position, GameObjectData?) -> Boolean>>

    final override val objectIds: IntArray

    init {
        val grouped = LinkedHashMap<Int, MutableList<(Client, Int, Position, GameObjectData?) -> Boolean>>()
        for (action in actions) {
            for (objectId in action.objectIds) {
                grouped.getOrPut(objectId) { ArrayList() } += action.handler
            }
        }
        actionsByObjectId = grouped
        objectIds = grouped.keys.sorted().toIntArray()
    }

    final override fun onFirstClick(context: ObjectInteractionContext): Boolean {
        val handlers = actionsByObjectId[context.objectId] ?: return false
        for (handler in handlers) {
            if (handler(context.player, context.objectId, context.position, context.definition)) {
                return true
            }
        }
        return false
    }
}