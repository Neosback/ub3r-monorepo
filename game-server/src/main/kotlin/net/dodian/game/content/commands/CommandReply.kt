package net.dodian.game.content.commands

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.persistence.audit.CommandLog

internal fun recordStaffCommand(client: Client, rawCommand: String) {
    CommandLog.recordCommand(client, rawCommand)
}
