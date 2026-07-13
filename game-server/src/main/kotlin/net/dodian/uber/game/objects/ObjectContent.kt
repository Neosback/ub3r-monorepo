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

    fun onFirstClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
    fun onSecondClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
    fun onThirdClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
    fun onFourthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
    fun onFifthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
    fun onUseItem(
        client: Client,
        objectId: Int,
        position: Position,
        obj: GameObjectData?,
        itemId: Int,
        itemSlot: Int,
        interfaceId: Int,
    ): Boolean = false
    fun onMagic(
        client: Client,
        objectId: Int,
        position: Position,
        obj: GameObjectData?,
        spellId: Int,
    ): Boolean = false

    fun onFirstClick(context: ObjectInteractionContext): Boolean =
        onFirstClick(context.player, context.objectId, context.position, context.definition)
    fun onSecondClick(context: ObjectInteractionContext): Boolean =
        onSecondClick(context.player, context.objectId, context.position, context.definition)
    fun onThirdClick(context: ObjectInteractionContext): Boolean =
        onThirdClick(context.player, context.objectId, context.position, context.definition)
    fun onFourthClick(context: ObjectInteractionContext): Boolean =
        onFourthClick(context.player, context.objectId, context.position, context.definition)
    fun onFifthClick(context: ObjectInteractionContext): Boolean =
        onFifthClick(context.player, context.objectId, context.position, context.definition)
    fun onUseItem(context: ObjectInteractionContext): Boolean =
        onUseItem(
            client = context.player,
            objectId = context.objectId,
            position = context.position,
            obj = context.definition,
            itemId = context.itemPayload?.itemId ?: -1,
            itemSlot = context.itemPayload?.itemSlot ?: -1,
            interfaceId = context.itemPayload?.interfaceId ?: -1
        )
    fun onMagic(context: ObjectInteractionContext): Boolean =
        onMagic(
            client = context.player,
            objectId = context.objectId,
            position = context.position,
            obj = context.definition,
            spellId = context.spellPayload?.spellId ?: -1
        )

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

