package net.dodian.uber.game.events.npc

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client


data class NpcClickEvent(
    val client: Client,
    val option: Int,
    val npc: Npc,
) : GameEvent