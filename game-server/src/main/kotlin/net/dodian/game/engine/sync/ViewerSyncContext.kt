package net.dodian.game.engine.sync

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.engine.sync.viewport.ViewportSnapshot

data class ViewerSyncContext(
    val viewer: Client,
    val snapshot: ViewportSnapshot?,
)
