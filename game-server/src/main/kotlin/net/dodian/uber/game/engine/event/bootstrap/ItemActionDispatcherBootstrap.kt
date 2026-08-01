package net.dodian.uber.game.engine.event.bootstrap

import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.events.item.ItemClickEvent
import net.dodian.uber.game.events.item.ItemOptionClickEvent
import net.dodian.uber.game.events.item.ItemOnItemEvent
import net.dodian.uber.game.events.item.ItemOnObjectEvent
import net.dodian.uber.game.events.item.ItemOnNpcEvent
import net.dodian.uber.game.engine.systems.interaction.items.ItemActionDispatcher

object ItemActionDispatcherBootstrap {
    @JvmStatic
    fun bootstrap() {
        GameEventBus.on<ItemClickEvent> { event ->
            ItemActionDispatcher.handleItemClick(event)
        }
        GameEventBus.on<ItemOptionClickEvent> { event ->
            ItemActionDispatcher.handleItemOptionClick(event)
        }
        GameEventBus.on<ItemOnItemEvent> { event ->
            ItemActionDispatcher.handleItemOnItem(event)
        }
        GameEventBus.on<ItemOnObjectEvent> { event ->
            ItemActionDispatcher.handleItemOnObject(event)
        }
        GameEventBus.on<ItemOnNpcEvent> { event ->
            ItemActionDispatcher.handleItemOnNpc(event)
        }
    }
}
