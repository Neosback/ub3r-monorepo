package net.dodian.game.engine.sync.player

data class PlayerSyncFastPathStats(
    val packetsBuilt: Int,
    val packetsSkipped: Int,
    val packetsTemplated: Int,
)
