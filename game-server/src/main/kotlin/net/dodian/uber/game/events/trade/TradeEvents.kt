package net.dodian.uber.game.events.trade

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class TradeRequestEvent(
    val requester: Client,
    val target: Client,
) : GameEvent


data class TradeCompleteEvent(
    val playerA: Client,
    val playerB: Client,
) : GameEvent


data class TradeCancelEvent(
    val canceller: Client,
    val other: Client,
) : GameEvent
