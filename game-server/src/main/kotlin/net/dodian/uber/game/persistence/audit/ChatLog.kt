package net.dodian.uber.game.persistence.audit

import net.dodian.uber.game.model.entity.player.Player

object ChatLog {
    @JvmStatic
    fun recordPublicChat(player: Player, message: String) {
        ConsoleAuditLog.publicChat(player, message)
    }

    @JvmStatic
    fun recordYellChat(player: Player, message: String) {
        ConsoleAuditLog.yellChat(player, message)
    }

    @JvmStatic
    fun recordPrivateChat(sender: Player, receiver: Player, message: String) {
        ConsoleAuditLog.privateChat(sender, receiver, message)
    }

    @JvmStatic
    fun recordModChat(player: Player, message: String) {
        ConsoleAuditLog.modChat(player, message)
    }

    @JvmStatic
    fun shutdown() {
        // No-op
    }
}