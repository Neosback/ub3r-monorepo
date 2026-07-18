package net.dodian.uber.game.engine.sync

import java.util.EnumMap
import net.dodian.uber.game.engine.sync.cache.RootSynchronizationCache
import net.dodian.uber.game.engine.sync.npc.NpcChunkActivityIndex
import net.dodian.uber.game.engine.sync.npc.RootNpcDeltaIndex
import net.dodian.uber.game.engine.sync.player.PlayerChunkActivityIndex
import net.dodian.uber.game.engine.sync.player.PlayerSyncRevisionIndex
import net.dodian.uber.game.engine.sync.viewport.ViewportIndex

class SynchronizationCycle(
    val tick: Long,
    val rootCache: RootSynchronizationCache,
    val viewportIndex: ViewportIndex?,
    val playerRevisionIndex: PlayerSyncRevisionIndex? = null,
    val playerActivityIndex: PlayerChunkActivityIndex? = null,
    val npcRevisionIndex: RootNpcDeltaIndex? = null,
    val npcActivityIndex: NpcChunkActivityIndex? = null,
) {
    private val stageDurationsNanos = EnumMap<SynchronizationStage, Long>(SynchronizationStage::class.java)

    var viewersEncoded: Int = 0
        private set
    var localPlayersWritten: Int = 0
        private set
    var localNpcsWritten: Int = 0
        private set
    var playerAddCount: Int = 0
        private set
    var npcAddCount: Int = 0
        private set
    var playerBlockCacheHits: Int = 0
        private set
    var playerBlockCacheMisses: Int = 0
        private set
    var playerBlockCacheEligible: Int = 0
        private set
    var npcBlockCacheHits: Int = 0
        private set
    var npcBlockCacheMisses: Int = 0
        private set
    var playerPacketsBuilt: Int = 0
        private set
    var playerScratchReuseCount: Int = 0
        private set
    var playerAppearanceCacheHits: Int = 0
        private set
    var playerAppearanceCacheMisses: Int = 0
        private set
    var playerLocalScans: Int = 0
        private set
    var npcPacketsBuilt: Int = 0
        private set
    var npcLocalScans: Int = 0
        private set

    fun recordStage(stage: SynchronizationStage, durationNanos: Long) {
        stageDurationsNanos[stage] = (stageDurationsNanos[stage] ?: 0L) + durationNanos
    }

    fun stageDurationNanos(stage: SynchronizationStage): Long = stageDurationsNanos[stage] ?: 0L

    fun recordViewer(localPlayers: Int, localNpcs: Int) {
        viewersEncoded++
        localPlayersWritten += localPlayers
        localNpcsWritten += localNpcs
    }

    fun recordPlayerAdd() {
        playerAddCount++
    }

    fun recordNpcAdd() {
        npcAddCount++
    }

    fun recordPlayerBlockCacheHit(hit: Boolean) {
        playerBlockCacheEligible++
        if (hit) {
            playerBlockCacheHits++
        } else {
            playerBlockCacheMisses++
        }
    }

    fun recordNpcBlockCacheHit(hit: Boolean) {
        if (hit) {
            npcBlockCacheHits++
        } else {
            npcBlockCacheMisses++
        }
    }

    fun recordPlayerPacketBuilt(localCount: Int) {
        playerPacketsBuilt++
        playerLocalScans += localCount
    }

    fun recordPlayerScratchReuse() {
        playerScratchReuseCount++
    }

    fun recordPlayerAppearanceCacheHit(hit: Boolean) {
        if (hit) {
            playerAppearanceCacheHits++
        } else {
            playerAppearanceCacheMisses++
        }
    }

    fun recordNpcPacketBuilt(localCount: Int) {
        npcPacketsBuilt++
        npcLocalScans += localCount
    }
}
