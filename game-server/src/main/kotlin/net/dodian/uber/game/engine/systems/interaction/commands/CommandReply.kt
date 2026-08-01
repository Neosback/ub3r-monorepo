package net.dodian.uber.game.engine.systems.interaction.commands

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.persistence.audit.ConsoleAuditLog

internal fun recordStaffCommand(client: Client, rawCommand: String) {
    ConsoleAuditLog.command(client, rawCommand)
}