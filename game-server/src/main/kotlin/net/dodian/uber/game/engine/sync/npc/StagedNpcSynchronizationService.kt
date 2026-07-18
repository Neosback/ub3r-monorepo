package net.dodian.uber.game.engine.sync.npc

import io.netty.buffer.ByteBuf
import java.util.Arrays
import net.dodian.uber.game.engine.metrics.OperationalTelemetry
import net.dodian.uber.game.engine.sync.SynchronizationContext
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.npc.NpcUpdating
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.codec.ByteMessage
import net.dodian.uber.game.netty.codec.MessageType

/** Stages protocol-compatible NPC local state and commits only after delivery acceptance. */
class StagedNpcSynchronizationService {
    private val updating = NpcUpdating.getInstance()
    private val scratch = ThreadLocal.withInitial(::Plan)
    private val resultScratch = ThreadLocal.withInitial(::Result)

    fun synchronize(viewer: Client): Result {
        val plan = buildPlan(viewer, scratch.get())
        val result = resultScratch.get()
        val capacity = maxOf(1024, viewer.npcUpdateCapacity)
        val pooledBuffer: ByteBuf = viewer.channel?.alloc()?.buffer(capacity) ?: ByteMessage.pooledBuffer(capacity)
        var message: ByteMessage? = null
        try {
            message = ByteMessage.message(65, MessageType.VAR_SHORT, pooledBuffer)
            encode(viewer, plan, message)
            val packetBytes = message.content().writerIndex()
            require(packetBytes <= MAX_VAR_SHORT_PAYLOAD) { "NPC synchronization payload exceeds protocol limit: $packetBytes" }
            viewer.updateNpcUpdateCapacity(packetBytes)
            OperationalTelemetry.incrementCounter("sync.npc.packet_bytes", packetBytes.toLong())
            OperationalTelemetry.incrementCounter("sync.npc.planned_additions", plan.additionCount.toLong())
            val accepted = viewer.sendRequiredSynchronization(message)
            message = null
            if (!accepted) {
                OperationalTelemetry.incrementCounter("sync.npc.required_enqueue_rejected")
                return result.set(false, packetBytes, plan.previousCount, plan.nextCount, plan.additionCount)
            }
            commit(viewer, plan)
            OperationalTelemetry.incrementCounter("sync.npc.staged_committed")
            OperationalTelemetry.incrementCounter("sync.npc.committed_locals", plan.nextCount.toLong())
            return result.set(true, packetBytes, plan.previousCount, plan.nextCount, plan.additionCount)
        } finally {
            message?.releaseAll()
        }
    }

    internal fun buildPlan(viewer: Client, plan: Plan = scratch.get()): Plan {
        plan.reset()
        plan.previousCount = viewer.localNpcSize.coerceIn(0, minOf(MAX_LOCALS, viewer.localNpcs.size))
        for (index in 0 until plan.previousCount) {
            val npc = viewer.localNpcs[index]
            plan.previous[index] = npc
            val retained = isRetained(viewer, npc)
            plan.retainPrevious[index] = retained
            if (retained && npc != null) plan.addNext(npc)
        }

        val nearby = SynchronizationContext.getViewportSnapshot(viewer)?.npcs
            ?: net.dodian.uber.game.Server.npcManager.getNpcs().toList()
        for (npc in nearby) {
            if (plan.additionCount >= MAX_ADDITIONS_PER_CYCLE || plan.nextCount >= MAX_LOCALS) break
            if (!isRetained(viewer, npc) || plan.contains(npc)) continue
            plan.addAddition(npc)
        }
        plan.freeze()
        return plan
    }

    internal fun encode(viewer: Client, plan: Plan, stream: ByteMessage) {
        plan.requireFrozen()
        val updateBlock = ByteMessage.raw(16384)
        try {
            stream.startBitAccess()
            stream.putBits(8, plan.previousCount)
            for (index in 0 until plan.previousCount) {
                val npc = plan.previous[index]
                if (!plan.retainPrevious[index] || npc == null) {
                    stream.putBits(1, 1)
                    stream.putBits(2, 3)
                } else {
                    updating.updateNPCMovement(npc, stream)
                    appendSharedOrEncode(npc, updateBlock)
                }
            }
            for (index in 0 until plan.additionCount) {
                val npc = plan.additions[index] ?: continue
                updating.addNpc(viewer, npc, stream)
                appendSharedOrEncode(npc, updateBlock)
                SynchronizationContext.recordNpcAdd()
            }
            if (updateBlock.buffer.writerIndex() > 0) {
                stream.putBits(NPC_SLOT_BITS, NPC_SLOT_TERMINATOR)
                stream.endBitAccess()
                stream.putBytes(updateBlock)
            } else {
                stream.endBitAccess()
            }
        } finally {
            updateBlock.releaseAll()
        }
    }

    private fun appendSharedOrEncode(npc: Npc, updateBlock: ByteMessage) {
        val sharedBlock = SynchronizationContext.getSharedNpcBlock(npc)
        if (sharedBlock != null) {
            updateBlock.putBytes(sharedBlock)
            SynchronizationContext.recordNpcBlockCacheHit(true)
        } else {
            updating.appendBlockUpdate(npc, updateBlock)
        }
    }

    internal fun commit(viewer: Client, plan: Plan) {
        plan.requireFrozen()
        var changed = viewer.localNpcSize != plan.nextCount
        if (!changed) {
            for (index in 0 until plan.nextCount) {
                if (viewer.localNpcs[index] !== plan.next[index]) {
                    changed = true
                    break
                }
            }
        }
        Arrays.fill(viewer.localNpcs, null)
        for (index in 0 until plan.nextCount) viewer.localNpcs[index] = plan.next[index]
        viewer.localNpcSize = plan.nextCount
        if (changed) viewer.bumpLocalNpcMembershipRevision()
    }

    private fun isRetained(viewer: Client, npc: Npc?): Boolean =
        npc != null && viewer.withinDistance(npc) && npc.isVisible && !NpcUpdating.removeNpc(viewer, npc)

    class Result internal constructor() {
        var accepted: Boolean = false
            private set
        var packetBytes: Int = 0
            private set
        var previousCount: Int = 0
            private set
        var committedCount: Int = 0
            private set
        var additions: Int = 0
            private set

        internal fun set(accepted: Boolean, packetBytes: Int, previousCount: Int, committedCount: Int, additions: Int): Result {
            this.accepted = accepted
            this.packetBytes = packetBytes
            this.previousCount = previousCount
            this.committedCount = committedCount
            this.additions = additions
            return this
        }
    }

    internal class Plan {
        val previous = arrayOfNulls<Npc>(MAX_LOCALS)
        val retainPrevious = BooleanArray(MAX_LOCALS)
        val additions = arrayOfNulls<Npc>(MAX_LOCALS)
        val next = arrayOfNulls<Npc>(MAX_LOCALS)
        val nextSlots = IntArray(MAX_LOCALS)
        var previousCount = 0
        var additionCount = 0
        var nextCount = 0
        private var frozen = false

        fun reset() {
            Arrays.fill(previous, null)
            Arrays.fill(retainPrevious, false)
            Arrays.fill(additions, null)
            Arrays.fill(next, null)
            previousCount = 0
            additionCount = 0
            nextCount = 0
            frozen = false
        }

        fun addNext(npc: Npc) {
            check(!frozen)
            next[nextCount] = npc
            nextSlots[nextCount] = npc.slot
            nextCount++
        }

        fun addAddition(npc: Npc) {
            additions[additionCount++] = npc
            addNext(npc)
        }

        fun contains(npc: Npc): Boolean {
            for (index in 0 until nextCount) if (next[index] === npc || nextSlots[index] == npc.slot) return true
            return false
        }

        fun freeze() { frozen = true }
        fun requireFrozen() = check(frozen) { "NPC synchronization plan is not frozen" }
    }

    companion object {
        private const val MAX_LOCALS = 255
        private const val MAX_ADDITIONS_PER_CYCLE = 25
        private const val MAX_VAR_SHORT_PAYLOAD = 65535
        private const val NPC_SLOT_BITS = 16
        private const val NPC_SLOT_TERMINATOR = (1 shl NPC_SLOT_BITS) - 1
    }
}
