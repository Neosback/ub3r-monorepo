package net.dodian.uber.game.events.magic

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class MagicOnPlayerEvent(
    val client: Client,
    val spellId: Int,
    val victimIndex: Int,
    val victim: Client,
) : GameEvent
