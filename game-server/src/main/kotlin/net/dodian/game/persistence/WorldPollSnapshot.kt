package net.dodian.game.persistence

import net.dodian.game.persistence.world.WorldPollInput
data class WorldPollSnapshot(
    val worldId: Int,
    val playerCount: Int,
    val onlinePlayerDbIds: IntArray,
) {
    fun toInput(): WorldPollInput = WorldPollInput(worldId, playerCount, onlinePlayerDbIds.asList())
}
