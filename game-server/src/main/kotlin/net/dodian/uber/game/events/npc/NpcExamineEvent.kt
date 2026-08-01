package net.dodian.uber.game.events.npc

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


@Suppress("unused")
data class NpcExamineEvent(
    val client: Client,
    val npcId: Int,
) : GameEvent