package net.dodian.uber.game.engine.sync

import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.sync.playerinfo.PlayerVisibilityRules
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.entity.player.PlayerUpdating
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.netty.codec.ByteMessage
import io.netty.channel.embedded.EmbeddedChannel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeterministicPlayerSynchronizationTest {
    private var previousItemManager: ItemManager? = null
    private val clients = mutableListOf<Client>()

    @BeforeEach
    fun setUp() {
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {})
        System.clearProperty("player.sync.mode")
    }

    @AfterEach
    fun tearDown() {
        clients.forEach { it.channel?.close() }
        clients.clear()
        System.clearProperty("player.sync.mode")
        PlayerRegistry.players.fill(null)
        PlayerRegistry.playersOnline.clear()
        Server.itemManager = previousItemManager
    }

    @Test
    fun `staged synchronization is the default and alternatives require opt in`() {
        assertEquals(PlayerSynchronizationMode.STAGED, PlayerSynchronizationMode.configured())
        System.setProperty("player.sync.mode", "canonical")
        assertEquals(PlayerSynchronizationMode.CANONICAL, PlayerSynchronizationMode.configured())
        System.setProperty("player.sync.mode", "optimized")
        assertEquals(PlayerSynchronizationMode.OPTIMIZED, PlayerSynchronizationMode.configured())
        System.setProperty("player.sync.mode", "unknown")
        assertEquals(PlayerSynchronizationMode.STAGED, PlayerSynchronizationMode.configured())
    }

    @Test
    fun `visibility requires both sessions to be fully synchronization ready`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val subject = client(2, "subject", 3201, 3200)

        subject.setSynchronizationReady(false)
        assertFalse(PlayerVisibilityRules.isVisibleTo(viewer, subject))

        subject.setSynchronizationReady(true)
        assertTrue(PlayerVisibilityRules.isVisibleTo(viewer, subject))
    }

    @Test
    fun `slot reuse removes old identity and admits replacement with forced update`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val oldSession = client(2, "old", 3201, 3200)
        val replacement = client(2, "replacement", 3201, 3200)
        replacement.equipment[Equipment.Slot.WEAPON.id] = 1277
        replacement.equipmentN[Equipment.Slot.WEAPON.id] = 1
        assertNotEquals(oldSession.synchronizationSessionGeneration, replacement.synchronizationSessionGeneration)

        viewer.playerList[0] = oldSession
        viewer.playerListSize = 1
        viewer.playersUpdating.add(oldSession)
        PlayerRegistry.players[viewer.slot] = viewer
        PlayerRegistry.players[replacement.slot] = replacement

        val packet = ByteMessage.raw(4096)
        try {
            PlayerUpdating.getInstance().update(viewer, packet)
            assertEquals(1, viewer.playerListSize)
            assertSame(replacement, viewer.playerList[0])
            assertTrue(viewer.playersUpdating.contains(replacement))
            assertTrue(packet.buffer.writerIndex() > 0)

            val bytes = packet.toByteArray()
            val bits = BitReader(bytes)
            assertEquals(0, bits.read(1), "stationary self must not enqueue a self mask")
            assertEquals(1, bits.read(8), "client must process the previous local before additions")
            assertEquals(1, bits.read(1))
            assertEquals(3, bits.read(2), "old slot identity must be removed")
            assertEquals(replacement.slot, bits.read(11))
            assertEquals(1, bits.read(1), "new local must always receive an update block")
            assertEquals(1, bits.read(1), "new local must discard stale client walking state")
            bits.read(5)
            bits.read(5)
            assertEquals(2047, bits.read(11))

            var byteIndex = bits.alignedByteIndex()
            assertEquals(0x10, bytes[byteIndex++].toInt() and 0xff, "add-local block must force appearance")
            val appearanceLength = (-bytes[byteIndex++].toInt()) and 0xff
            assertTrue(appearanceLength > 0)
            val appearanceStart = byteIndex
            var componentIndex = appearanceStart + 4 // gender, head icon, skull icon, bounty icon
            repeat(3) { componentIndex = nextAppearanceComponent(bytes, componentIndex).second }
            val weapon = nextAppearanceComponent(bytes, componentIndex).first
            assertEquals(0x200 + 1277, weapon, "observer must receive the replacement session's current weapon")
            byteIndex += appearanceLength
            assertEquals(bytes.size, byteIndex, "the active client's packet parser must consume the complete payload")
        } finally {
            packet.releaseAll()
        }
    }

    private fun client(slot: Int, name: String, x: Int, y: Int): Client =
        Client(EmbeddedChannel(), slot).apply {
            playerName = name
            moveTo(x, y, 0)
            loaded = true
            initialized = true
            isActive = true
            setSynchronizationReady(true)
            clients += this
        }

    private class BitReader(private val bytes: ByteArray) {
        private var bitIndex = 0

        fun read(count: Int): Int {
            var value = 0
            repeat(count) {
                val byte = bytes[bitIndex ushr 3].toInt() and 0xff
                value = (value shl 1) or ((byte ushr (7 - (bitIndex and 7))) and 1)
                bitIndex++
            }
            return value
        }

        fun alignedByteIndex(): Int = (bitIndex + 7) ushr 3
    }

    private fun nextAppearanceComponent(bytes: ByteArray, index: Int): Pair<Int, Int> {
        val high = bytes[index].toInt() and 0xff
        if (high == 0) return 0 to (index + 1)
        return ((high shl 8) or (bytes[index + 1].toInt() and 0xff)) to (index + 2)
    }
}
