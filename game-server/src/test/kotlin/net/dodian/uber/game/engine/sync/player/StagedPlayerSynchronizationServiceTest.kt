package net.dodian.uber.game.engine.sync.player

import io.netty.channel.embedded.EmbeddedChannel
import kotlin.random.Random
import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.entity.player.PlayerUpdating
import net.dodian.uber.game.netty.codec.ByteMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StagedPlayerSynchronizationServiceTest {
    private val clients = mutableListOf<Client>()
    private var previousItemManager: ItemManager? = null

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
    fun `staged encoder matches canonical packet for the same snapshot`() {
        val canonicalViewer = client(1, "viewer", 3200, 3200)
        val canonicalSubject = client(2, "subject", 3201, 3200)
        PlayerRegistry.players[1] = canonicalViewer
        PlayerRegistry.players[2] = canonicalSubject
        val canonical = ByteMessage.raw(4096)
        PlayerUpdating.getInstance().update(canonicalViewer, canonical)
        val canonicalBytes = canonical.toByteArray()
        canonical.releaseAll()

        PlayerRegistry.players.fill(null)
        val stagedViewer = client(1, "viewer", 3200, 3200)
        val stagedSubject = client(2, "subject", 3201, 3200)
        PlayerRegistry.players[1] = stagedViewer
        PlayerRegistry.players[2] = stagedSubject
        val service = StagedPlayerSynchronizationService()
        val plan = service.buildPlan(stagedViewer)
        val staged = ByteMessage.raw(4096)
        service.encode(stagedViewer, plan, staged)
        val stagedBytes = staged.toByteArray()
        staged.releaseAll()

        assertArrayEquals(canonicalBytes, stagedBytes)
        assertEquals(0, stagedViewer.playerListSize, "encoding must not commit viewer state")
        service.commit(stagedViewer, plan)
        assertEquals(1, stagedViewer.playerListSize)
        assertSame(stagedSubject, stagedViewer.playerList[0])
    }

    @Test
    fun `stale slot replacement remains unchanged until commit`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val old = client(2, "old", 3201, 3200)
        val replacement = client(2, "replacement", 3201, 3200)
        viewer.playerList[0] = old
        viewer.playerListSize = 1
        viewer.playersUpdating.add(old)
        PlayerRegistry.players[1] = viewer
        PlayerRegistry.players[2] = replacement

        val service = StagedPlayerSynchronizationService()
        val plan = service.buildPlan(viewer)
        val packet = ByteMessage.raw(4096)
        service.encode(viewer, plan, packet)
        packet.releaseAll()

        assertSame(old, viewer.playerList[0])
        assertEquals(1, viewer.playerListSize)
        service.commit(viewer, plan)
        assertSame(replacement, viewer.playerList[0])
    }

    @Test
    fun `all ninety nine visible players fit one staged admission`() {
        val viewer = client(1, "viewer", 3200, 3200)
        PlayerRegistry.players[1] = viewer
        for (slot in 2..100) {
            PlayerRegistry.players[slot] = client(slot, "p$slot", 3200 + (slot % 8), 3200 + (slot % 7))
        }

        val service = StagedPlayerSynchronizationService()
        val plan = service.buildPlan(viewer)
        assertEquals(99, plan.additionCount)
        assertEquals(99, plan.nextCount)
        val packet = ByteMessage.raw(65535)
        service.encode(viewer, plan, packet)
        assertTrue(packet.buffer.writerIndex() in 1..65535)
        packet.releaseAll()
    }

    @Test
    fun `required enqueue rejection leaves viewer local state uncommitted`() {
        val viewer = client(1, "viewer", 3200, 3200)
        val subject = client(2, "subject", 3201, 3200)
        PlayerRegistry.players[1] = viewer
        PlayerRegistry.players[2] = subject
        repeat(1024) { viewer.send(ByteMessage.raw(1)) }

        val result = StagedPlayerSynchronizationService().synchronize(viewer)

        assertTrue(!result.accepted)
        assertEquals(0, viewer.playerListSize)
        assertTrue(viewer.playersUpdating.isEmpty)
    }

    @Test
    fun `randomized 317 local sets remain byte compatible with canonical encoder`() {
        repeat(24) { seed ->
            val canonical = encodeScenario(seed, staged = false)
            PlayerRegistry.players.fill(null)
            PlayerRegistry.playersOnline.clear()
            val staged = encodeScenario(seed, staged = true)
            assertArrayEquals(canonical, staged, "seed=$seed")
            PlayerRegistry.players.fill(null)
            PlayerRegistry.playersOnline.clear()
        }
    }

    private fun encodeScenario(seed: Int, staged: Boolean): ByteArray {
        val random = Random(seed)
        val viewer = client(1, "viewer-$seed", 3200, 3200)
        PlayerRegistry.players[1] = viewer
        val subjects = (2..14).associateWith { slot ->
            client(slot, "subject-$slot", 3200 + slot - 1, 3200)
        }
        val previousCount = random.nextInt(0, 6)
        repeat(previousCount) { index ->
            val subject = subjects.getValue(index + 2)
            viewer.playerList[index] = subject
            viewer.playersUpdating.add(subject)
        }
        viewer.playerListSize = previousCount
        subjects.forEach { (slot, subject) ->
            if (slot > previousCount + 1 || random.nextBoolean()) {
                PlayerRegistry.players[slot] = subject
            }
            if (random.nextInt(4) == 0) subject.faceTarget(random.nextInt(0, 2047))
        }

        val packet = ByteMessage.raw(8192)
        if (staged) {
            val service = StagedPlayerSynchronizationService()
            val plan = service.buildPlan(viewer)
            service.encode(viewer, plan, packet)
        } else {
            PlayerUpdating.getInstance().update(viewer, packet)
        }
        return packet.toByteArray().also { packet.releaseAll() }
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
