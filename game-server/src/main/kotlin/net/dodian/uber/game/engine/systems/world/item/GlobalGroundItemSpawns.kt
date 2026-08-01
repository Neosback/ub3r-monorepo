package net.dodian.uber.game.engine.systems.world.item

import net.dodian.uber.game.model.Position

data class GroundItemSpawn(
    val position: Position,
    val itemId: Int,
    val amount: Int,
    val displayTime: Int,
)

object GlobalGroundItemSpawns {
    @JvmField
    val spawns: List<GroundItemSpawn> = TomlGroundItemSpawnLoader.load()

    @JvmStatic
    fun spawnAll() {
        for (spawn in spawns) {
            Ground.addGroundItem(spawn.position, spawn.itemId, spawn.amount, spawn.displayTime)
        }
    }
}
