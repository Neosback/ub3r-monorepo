package net.dodian.uber.game.persistence.audit

import java.util.concurrent.CopyOnWriteArrayList
import net.dodian.uber.game.model.item.GameItem

object TradeLog {
    @JvmStatic
    fun recordTrade(
        p1: Int,
        p2: Int,
        items: CopyOnWriteArrayList<GameItem>,
        otherItems: CopyOnWriteArrayList<GameItem>,
        trade: Boolean,
    ) {
        ConsoleAuditLog.trade(p1, p2, items, otherItems, trade)
    }
}