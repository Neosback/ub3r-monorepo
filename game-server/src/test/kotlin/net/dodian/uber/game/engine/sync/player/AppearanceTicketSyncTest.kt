package net.dodian.uber.game.engine.sync.player

import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.netty.codec.ByteMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Apollo-style appearance tickets: the 377 client caches appearance per player index for the
 * whole session and re-applies it automatically when a player is re-added, so a re-add whose
 * appearance the viewer has already received skips the appearance block entirely.
 */
class AppearanceTicketSyncTest {
    private var previousItemManager: ItemManager? = null
    private val clients = mutableListOf<Client>()

    @BeforeEach
    fun setUp() {
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {})
    }

    @AfterEach
    fun tearDown() {
        clients.forEach {
            it.saveNeeded = false
            it.destruct()
        }
        clients.clear()
        PlayerRegistry.players.fill(null)
        PlayerRegistry.playersOnline.clear()
        Server.itemManager = previousItemManager
    }

    @Test
    fun `re-add after leaving view skips the appearance block`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val subject = client(2, "subject", 3201, 3200)
        PlayerRegistry.players[1] = viewer
        PlayerRegistry.players[2] = subject
        val service = StagedPlayerSynchronizationService()

        val firstAdd = encodeAndCommit(service, viewer)
        simulateSubjectLeftView(viewer)
        val readd = encodeAndCommit(service, viewer)

        assertTrue(firstAdd.length > readd.length, "re-add must be smaller than the first add")
        // First sight carries the full appearance block (mask 0x10 + length-prefixed payload).
        assertEquals(
            "00002c01ffe010bb00ffffff00000000011200011a012601030122012a010e" +
                "020e05040003280333033303330333033303380000000bb0b4ea280a000000" +
                "000a0a0a3ff0000000000000000000",
            firstAdd,
        )
        // Re-add with a matching ticket: 11-bit slot, hasBlock=0, discardQueue=1, deltas — no
        // byte payload at all.
        assertEquals("00002401ffe0", readd)
    }

    @Test
    fun `appearance change between sightings re-sends the appearance block`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val subject = client(2, "subject", 3201, 3200)
        PlayerRegistry.players[1] = viewer
        PlayerRegistry.players[2] = subject
        val service = StagedPlayerSynchronizationService()

        val firstAdd = encodeAndCommit(service, viewer)
        simulateSubjectLeftView(viewer)

        // Raw equipment write: invalidates the appearance cache via the defensive signature
        // check without setting the APPEARANCE flag — the ticket must still be treated as stale.
        subject.equipment[Equipment.Slot.WEAPON.id] = 1277

        val readd = encodeAndCommit(service, viewer)
        assertNotEquals(firstAdd, readd)
        assertTrue(readd.length >= firstAdd.length, "changed appearance must be re-sent in full")
    }

    @Test
    fun `unchanged appearance keeps skipping across repeated re-adds`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val subject = client(2, "subject", 3201, 3200)
        PlayerRegistry.players[1] = viewer
        PlayerRegistry.players[2] = subject
        val service = StagedPlayerSynchronizationService()

        encodeAndCommit(service, viewer)
        simulateSubjectLeftView(viewer)
        val firstReadd = encodeAndCommit(service, viewer)
        simulateSubjectLeftView(viewer)
        val secondReadd = encodeAndCommit(service, viewer)

        assertEquals(firstReadd, secondReadd)
        assertEquals("00002401ffe0", secondReadd)
    }

    private fun encodeAndCommit(service: StagedPlayerSynchronizationService, viewer: Client): String {
        val plan = service.buildPlan(viewer)
        val packet = ByteMessage.wrap(Unpooled.buffer(8192))
        service.encode(viewer, plan, packet)
        val hex = packet.toByteArray().joinToString("") { "%02x".format(it) }
        packet.releaseAll()
        service.commit(viewer, plan)
        return hex
    }

    /** Mimics an earlier tick having removed the subject from the viewer's local list. */
    private fun simulateSubjectLeftView(viewer: Client) {
        java.util.Arrays.fill(viewer.playerList, null)
        viewer.playerListSize = 0
        viewer.playersUpdating.clear()
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
}
