package net.dodian.uber.game.engine.systems.interaction.items

import net.dodian.uber.game.api.content.ContentErrorPolicy
import net.dodian.uber.game.events.item.ItemClickEvent
import net.dodian.uber.game.events.item.ItemOptionClickEvent
import net.dodian.uber.game.events.item.ItemOnItemEvent
import net.dodian.uber.game.events.item.ItemOnObjectEvent
import net.dodian.uber.game.events.item.ItemOnNpcEvent

object ItemActionDispatcher {
    fun handleItemClick(event: ItemClickEvent): Boolean {
        val content = ItemContentRegistry.get(event.itemId) ?: return false
        return ContentErrorPolicy.runBoolean(event.client, "item.dispatch.click", bindingKey = "item.click:${event.itemId}:1") {
            content.onFirstClick(event.client, event.itemId, event.itemSlot, event.interfaceId)
        }
    }

    fun handleItemOptionClick(event: ItemOptionClickEvent): Boolean {
        val content = ItemContentRegistry.get(event.itemId) ?: return false
        return ContentErrorPolicy.runBoolean(event.client, "item.dispatch.option.${event.option}", bindingKey = "item.click:${event.itemId}:${event.option}") {
            when (event.option) {
                2 -> content.onSecondClick(event.client, event.itemId, event.itemSlot, event.interfaceId)
                3 -> content.onThirdClick(event.client, event.itemId, event.itemSlot, event.interfaceId)
                else -> false
            }
        }
    }

    fun handleItemOnItem(event: ItemOnItemEvent): Boolean {
        val contentUsed = ItemContentRegistry.get(event.itemUsedId)
        if (contentUsed != null) {
            val handled = ContentErrorPolicy.runBoolean(event.client, "item.dispatch.item_on_item.used", bindingKey = "item.on-item:${event.itemUsedId}:${event.itemUsedWithId}") {
                contentUsed.onItemOnItem(
                    client = event.client,
                    itemId = event.itemUsedId,
                    itemSlot = event.itemUsedSlot,
                    otherItemId = event.itemUsedWithId,
                    otherItemSlot = event.itemUsedWithSlot
                )
            }
            if (handled) return true
        }

        val contentWith = ItemContentRegistry.get(event.itemUsedWithId)
        if (contentWith != null) {
            val handled = ContentErrorPolicy.runBoolean(event.client, "item.dispatch.item_on_item.with", bindingKey = "item.on-item:${event.itemUsedWithId}:${event.itemUsedId}") {
                contentWith.onItemOnItem(
                    client = event.client,
                    itemId = event.itemUsedWithId,
                    itemSlot = event.itemUsedWithSlot,
                    otherItemId = event.itemUsedId,
                    otherItemSlot = event.itemUsedSlot
                )
            }
            if (handled) return true
        }

        return false
    }

    fun handleItemOnObject(event: ItemOnObjectEvent): Boolean {
        val content = ItemContentRegistry.get(event.itemId) ?: return false
        return ContentErrorPolicy.runBoolean(event.client, "item.dispatch.item_on_object", bindingKey = "item.on-object:${event.itemId}:${event.objectId}") {
            content.onItemOnObject(
                client = event.client,
                itemId = event.itemId,
                itemSlot = event.itemSlot,
                objectId = event.objectId,
                position = event.position,
                obj = event.obj
            )
        }
    }

    fun handleItemOnNpc(event: ItemOnNpcEvent): Boolean {
        val content = ItemContentRegistry.get(event.itemId) ?: return false
        return ContentErrorPolicy.runBoolean(event.client, "item.dispatch.item_on_npc", bindingKey = "item.on-npc:${event.itemId}:${event.npc.id}") {
            content.onItemOnNpc(
                client = event.client,
                itemId = event.itemId,
                itemSlot = event.itemSlot,
                npc = event.npc
            )
        }
    }
}
