package net.dodian.uber.game.events.item

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client


data class ItemOnNpcEvent(
    val client: Client,
    val itemId: Int,
    val itemSlot: Int,
    val npcIndex: Int,
    val npc: Npc,
) : GameEvent
