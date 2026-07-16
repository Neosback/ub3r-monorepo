package net.dodian.uber.game.engine.sync.npc

import io.netty.channel.embedded.EmbeddedChannel
import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.systems.world.npc.NpcManager
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.npc.NpcUpdating
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.codec.ByteMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StagedNpcSynchronizationServiceTest {
    private var previousNpcManager: NpcManager? = null
    private val channels = mutableListOf<EmbeddedChannel>()
    private val clients = mutableListOf<Client>()

    @BeforeEach
    fun setUp() {
        previousNpcManager = Server.npcManager
        Server.npcManager = NpcManager()
    }

    @AfterEach
    fun tearDown() {
        clients.forEach {
            it.saveNeeded = false
            it.destruct()
        }
        clients.clear()
        channels.forEach(EmbeddedChannel::close)
        channels.clear()
        Server.npcManager = previousNpcManager
    }

    @Test
    fun `staged npc encoder matches canonical and commits after encoding`() {
        val canonicalViewer = client(1)
        val canonicalNpc = Npc(1, 1, Position(3201, 3200, 0), 0)
        Server.npcManager.npcMap[canonicalNpc.slot] = canonicalNpc
        val canonical = ByteMessage.raw(4096)
        NpcUpdating.getInstance().update(canonicalViewer, canonical)
        val canonicalBytes = canonical.toByteArray()
        canonical.releaseAll()

        Server.npcManager.npcMap.clear()
        val stagedViewer = client(2)
        val stagedNpc = Npc(1, 1, Position(3201, 3200, 0), 0)
        Server.npcManager.npcMap[stagedNpc.slot] = stagedNpc
        val service = StagedNpcSynchronizationService()
        val plan = service.buildPlan(stagedViewer)
        val staged = ByteMessage.raw(4096)
        service.encode(stagedViewer, plan, staged)
        val stagedBytes = staged.toByteArray()
        staged.releaseAll()

        assertArrayEquals(canonicalBytes, stagedBytes)
        assertEquals(0, stagedViewer.localNpcSize)
        service.commit(stagedViewer, plan)
        assertEquals(1, stagedViewer.localNpcSize)
        assertSame(stagedNpc, stagedViewer.localNpcs[0])
    }

    @Test
    fun `npc encoding failure leaves viewer state uncommitted`() {
        val viewer = client(1)
        val invalidSlotNpc = Npc(16383, 1, Position(3201, 3200, 0), 0)
        Server.npcManager.npcMap[invalidSlotNpc.slot] = invalidSlotNpc
        val service = StagedNpcSynchronizationService()
        val plan = service.buildPlan(viewer)
        val packet = ByteMessage.raw(4096)

        assertThrows(IllegalArgumentException::class.java) { service.encode(viewer, plan, packet) }
        packet.releaseAll()
        assertEquals(0, viewer.localNpcSize)
    }

    @Test
    fun `required npc enqueue rejection leaves viewer state uncommitted`() {
        val viewer = client(1)
        val npc = Npc(1, 1, Position(3201, 3200, 0), 0)
        Server.npcManager.npcMap[npc.slot] = npc
        repeat(1024) { viewer.send(ByteMessage.raw(1)) }

        val result = StagedNpcSynchronizationService().synchronize(viewer)

        assertTrue(!result.accepted)
        assertEquals(0, viewer.localNpcSize)
    }

    private fun client(slot: Int): Client {
        val channel = EmbeddedChannel()
        channels += channel
        return Client(channel, slot).apply {
            playerName = "viewer$slot"
            moveTo(3200, 3200, 0)
            loaded = true
            initialized = true
            isActive = true
            setSynchronizationReady(true)
            clients += this
        }
    }
}
