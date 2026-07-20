package net.dodian.uber.game.engine.systems.world.item

import net.dodian.uber.game.model.Position
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GlobalGroundItemSpawnsTest {
    @Test
    fun `loads all spawns from the toml file`() {
        val spawns = TomlGroundItemSpawnLoader.load()
        assertEquals(48, spawns.size)
    }

    @Test
    fun `first and last entries match the original hardcoded data`() {
        val spawns = TomlGroundItemSpawnLoader.load()
        val first = spawns.first()
        assertEquals(Position(2611, 3096, 0), first.position)
        assertEquals(11862, first.itemId)
        assertEquals(1, first.amount)
        assertEquals(100, first.displayTime)

        val last = spawns.last()
        assertEquals(Position(2642, 3240, 0), last.position)
        assertEquals(401, last.itemId)
        assertEquals(1, last.amount)
        assertEquals(25, last.displayTime)
    }

    @Test
    fun `spawnAll entry point still exposes the loaded list`() {
        assertEquals(48, GlobalGroundItemSpawns.spawns.size)
    }
}
