package net.dodian.uber.game.item

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

interface ItemContent {
    val itemIds: IntArray

    fun onFirstClick(client: Client, itemId: Int, itemSlot: Int, interfaceId: Int): Boolean = false
    fun onSecondClick(client: Client, itemId: Int, itemSlot: Int, interfaceId: Int): Boolean = false
    fun onThirdClick(client: Client, itemId: Int, itemSlot: Int, interfaceId: Int): Boolean = false

    fun onItemOnItem(client: Client, itemId: Int, itemSlot: Int, otherItemId: Int, otherItemSlot: Int): Boolean = false
    fun onItemOnObject(client: Client, itemId: Int, itemSlot: Int, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
    fun onItemOnNpc(client: Client, itemId: Int, itemSlot: Int, npc: Npc): Boolean = false
}
