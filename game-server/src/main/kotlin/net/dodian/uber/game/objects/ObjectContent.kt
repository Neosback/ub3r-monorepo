package net.dodian.uber.game.objects

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.api.content.ContentObjectInteractionPolicy

import net.dodian.uber.game.api.interaction.ObjectInteractionContext

interface ObjectContent {
    val objectIds: IntArray
        get() = intArrayOf()

    fun bindings(): List<ObjectBinding> {
        return objectIds
            .sorted()
            .map { ObjectBinding(objectId = it) }
    }

    fun onFirstClick(context: ObjectInteractionContext): Boolean = false
    fun onSecondClick(context: ObjectInteractionContext): Boolean = false
    fun onThirdClick(context: ObjectInteractionContext): Boolean = false
    fun onFourthClick(context: ObjectInteractionContext): Boolean = false
    fun onFifthClick(context: ObjectInteractionContext): Boolean = false
    fun onUseItem(context: ObjectInteractionContext): Boolean = false
    fun onMagic(context: ObjectInteractionContext): Boolean = false

    fun clickInteractionPolicy(
        option: Int,
        objectId: Int,
        position: Position,
        obj: GameObjectData?,
    ): ContentObjectInteractionPolicy? = null

    fun itemOnObjectInteractionPolicy(
        objectId: Int,
        position: Position,
        obj: GameObjectData?,
        itemId: Int,
        itemSlot: Int,
        interfaceId: Int,
    ): ContentObjectInteractionPolicy? = null

    fun magicOnObjectInteractionPolicy(
        objectId: Int,
        position: Position,
        obj: GameObjectData?,
        spellId: Int,
    ): ContentObjectInteractionPolicy? = null
}

