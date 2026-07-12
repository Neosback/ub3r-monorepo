package net.dodian.uber.game.engine.sync.viewport

import java.util.IdentityHashMap
import net.dodian.uber.game.Server
import net.dodian.uber.game.model.EntityType
import net.dodian.uber.game.model.chunk.ChunkEntityIndex
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.entity.player.Player
import net.dodian.uber.game.engine.sync.util.LongObjectMap

object ViewportSnapshotPool {
    private val playerListPool = ArrayDeque<ArrayList<Player>>()
    private val npcListPool = ArrayDeque<ArrayList<Npc>>()

    fun rentPlayerList(): ArrayList<Player> {
        return if (playerListPool.isNotEmpty()) playerListPool.removeLast() else ArrayList(256)
    }

    fun rentNpcList(): ArrayList<Npc> {
        return if (npcListPool.isNotEmpty()) npcListPool.removeLast() else ArrayList(256)
    }

    fun release(players: ArrayList<Player>, npcs: ArrayList<Npc>) {
        players.clear()
        npcs.clear()
        playerListPool.addLast(players)
        npcListPool.addLast(npcs)
    }
}

class ViewportIndex private constructor(
    private val snapshots: IdentityHashMap<Player, ViewportSnapshot>,
    private val relevantNpcs: List<Npc>,
) {
    fun snapshotFor(viewer: Player): ViewportSnapshot? = snapshots[viewer]
    fun relevantNpcs(): List<Npc> = relevantNpcs

    fun release() {
        snapshots.values.distinct().forEach { snapshot ->
            ViewportSnapshotPool.release(snapshot.players as ArrayList<Player>, snapshot.npcs as ArrayList<Npc>)
        }
    }

    companion object {
        fun build(viewers: List<Client>, distance: Int): ViewportIndex? {
            val chunkManager = Server.chunkManager ?: return null
            val neighborhoodCache = LongObjectMap<ViewportSnapshot>(viewers.size.coerceAtLeast(16))
            val snapshots = IdentityHashMap<Player, ViewportSnapshot>(viewers.size)
            val relevantNpcSeen = IdentityHashMap<Npc, Boolean>()
            val relevantNpcs = ArrayList<Npc>()
            viewers.forEach { viewer ->
                val position = viewer.position ?: return@forEach
                val centerChunkX = position.chunkX
                val centerChunkY = position.chunkY
                val neighborhoodKey = pack(centerChunkX, centerChunkY, position.z)
                val snapshot =
                    neighborhoodCache.getOrPut(neighborhoodKey) {
                        buildNeighborhood(chunkManager, position, distance)
                    }
                snapshots[viewer] = snapshot
                snapshot.npcs.forEach { npc ->
                    if (relevantNpcSeen.put(npc, true) == null) {
                        relevantNpcs += npc
                    }
                }
            }
            return ViewportIndex(snapshots, relevantNpcs)
        }

        private fun buildNeighborhood(
            chunkManager: net.dodian.uber.game.model.chunk.ChunkManager,
            center: net.dodian.uber.game.model.Position,
            distance: Int,
        ): ViewportSnapshot {
            val level = center.z
            val players = ViewportSnapshotPool.rentPlayerList()
            val npcs = ViewportSnapshotPool.rentNpcList()
            chunkManager.forEachViewableChunk(center, distance) { repo: ChunkEntityIndex ->
                for (other in repo.getAll<Player>(EntityType.PLAYER)) {
                    if (other.isActive && other.position?.z == level) {
                        players += other
                    }
                }
                for (npc in repo.getAll<Npc>(EntityType.NPC)) {
                    if (npc.isVisible && npc.position?.z == level) {
                        npcs += npc
                    }
                }
            }
            return ViewportSnapshot(players, npcs)
        }

        private fun pack(
            chunkX: Int,
            chunkY: Int,
            level: Int,
        ): Long = (((chunkX.toLong() and 0x1fffffL) shl 43) or ((chunkY.toLong() and 0x1fffffL) shl 22) or (level.toLong() and 0x3fffffL))
    }
}