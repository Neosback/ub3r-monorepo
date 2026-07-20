package net.dodian.uber.game.engine.net

import java.util.ArrayDeque
import net.dodian.uber.game.netty.game.GamePacket

/**
 * Per-client inbound mailbox that preserves ordering for transactional packets
 * while collapsing superseding input families.
 */
class InboundPacketMailbox(maxPendingPackets: Int) {
    enum class Family {
        FIFO,
        WALK,
        MOUSE,
        ITEM_CLICK,
    }

    class EnqueueResult private constructor(
        private val accepted: Boolean,
        private val family: Family,
    ) {
        fun accepted(): Boolean = accepted

        fun family(): Family = family

        companion object {
            @JvmStatic
            fun of(accepted: Boolean, family: Family): EnqueueResult = EnqueueResult(accepted, family)
        }
    }

    class MailboxCounters private constructor(
        private val walkReplaced: Int,
        private val mouseReplaced: Int,
        private val itemClickReplaced: Int,
        private val fifoDropped: Int,
    ) {
        fun walkReplaced(): Int = walkReplaced

        fun mouseReplaced(): Int = mouseReplaced

        fun itemClickReplaced(): Int = itemClickReplaced

        fun fifoDropped(): Int = fifoDropped

        companion object {
            private val EMPTY = MailboxCounters(0, 0, 0, 0)

            @JvmStatic
            fun empty(): MailboxCounters = EMPTY

            @JvmStatic
            fun of(walkReplaced: Int, mouseReplaced: Int, itemClickReplaced: Int, fifoDropped: Int): MailboxCounters =
                if (walkReplaced == 0 && mouseReplaced == 0 && itemClickReplaced == 0 && fifoDropped == 0) {
                    EMPTY
                } else {
                    MailboxCounters(walkReplaced, mouseReplaced, itemClickReplaced, fifoDropped)
                }
        }
    }

    class PollResult private constructor(
        private val packet: GamePacket,
        private val family: Family,
    ) {
        fun packet(): GamePacket = packet

        fun family(): Family = family

        companion object {
            @JvmStatic
            fun of(packet: GamePacket, family: Family): PollResult = PollResult(packet, family)
        }
    }

    fun interface PacketReleaser {
        fun release(packet: GamePacket?)
    }

    private class SequencedPacket(
        val sequence: Long,
        val family: Family,
        val packet: GamePacket,
    )

    private val maxPendingPackets = maxOf(1, maxPendingPackets)
    private val transactionalPackets = ArrayDeque<SequencedPacket>()

    private var nextSequence = 0L
    private var pendingCount = 0

    private var walkPacket: SequencedPacket? = null
    private var mousePacket: SequencedPacket? = null
    private var itemClickPacket: SequencedPacket? = null

    private var walkReplacedSinceSnapshot = 0
    private var mouseReplacedSinceSnapshot = 0
    private var itemClickReplacedSinceSnapshot = 0
    private var fifoDroppedSinceSnapshot = 0

    @Synchronized
    fun enqueue(packet: GamePacket?): EnqueueResult {
        if (packet == null) {
            return EnqueueResult.of(false, Family.FIFO)
        }
        val family = familyOf(packet.opcode())
        val sequenced = SequencedPacket(++nextSequence, family, packet)
        return when (family) {
            Family.WALK, Family.MOUSE, Family.ITEM_CLICK -> {
                replaceSupersedingPacket(sequenced, family)
                EnqueueResult.of(true, family)
            }
            Family.FIFO -> {
                if (pendingCount >= maxPendingPackets) {
                    fifoDroppedSinceSnapshot++
                    EnqueueResult.of(false, family)
                } else {
                    transactionalPackets.addLast(sequenced)
                    pendingCount++
                    EnqueueResult.of(true, family)
                }
            }
        }
    }

    @Synchronized
    fun pollNext(): PollResult? {
        val transactional = transactionalPackets.pollFirst()
        if (transactional != null) {
            pendingCount--
            return PollResult.of(transactional.packet, transactional.family)
        }

        val currentWalk = walkPacket
        if (currentWalk != null) {
            walkPacket = null
            pendingCount--
            return PollResult.of(currentWalk.packet, currentWalk.family)
        }

        val currentItemClick = itemClickPacket
        if (currentItemClick != null) {
            itemClickPacket = null
            pendingCount--
            return PollResult.of(currentItemClick.packet, currentItemClick.family)
        }

        val currentMouse = mousePacket
        if (currentMouse != null) {
            mousePacket = null
            pendingCount--
            return PollResult.of(currentMouse.packet, currentMouse.family)
        }

        return null
    }

    @Synchronized
    fun pendingCount(): Int = pendingCount

    @Synchronized
    fun snapshotAndResetCounters(): MailboxCounters {
        val counters = MailboxCounters.of(
            walkReplacedSinceSnapshot,
            mouseReplacedSinceSnapshot,
            itemClickReplacedSinceSnapshot,
            fifoDroppedSinceSnapshot,
        )
        walkReplacedSinceSnapshot = 0
        mouseReplacedSinceSnapshot = 0
        itemClickReplacedSinceSnapshot = 0
        fifoDroppedSinceSnapshot = 0
        return counters
    }

    @Synchronized
    fun clear(releaser: PacketReleaser) {
        while (!transactionalPackets.isEmpty()) {
            releaser.release(transactionalPackets.removeFirst().packet)
        }
        val currentWalk = walkPacket
        if (currentWalk != null) {
            releaser.release(currentWalk.packet)
            walkPacket = null
        }
        val currentMouse = mousePacket
        if (currentMouse != null) {
            releaser.release(currentMouse.packet)
            mousePacket = null
        }
        val currentItemClick = itemClickPacket
        if (currentItemClick != null) {
            releaser.release(currentItemClick.packet)
            itemClickPacket = null
        }
        pendingCount = 0
    }

    private fun replaceSupersedingPacket(packet: SequencedPacket, family: Family) {
        val previous = when (family) {
            Family.WALK -> walkPacket
            Family.MOUSE -> mousePacket
            Family.ITEM_CLICK -> itemClickPacket
            Family.FIFO -> null
        }
        if (previous == null) {
            pendingCount++
        } else {
            when (family) {
                Family.WALK -> walkReplacedSinceSnapshot++
                Family.MOUSE -> mouseReplacedSinceSnapshot++
                Family.ITEM_CLICK -> itemClickReplacedSinceSnapshot++
                Family.FIFO -> Unit
            }
            release(previous.packet)
        }
        when (family) {
            Family.WALK -> walkPacket = packet
            Family.MOUSE -> mousePacket = packet
            Family.ITEM_CLICK -> itemClickPacket = packet
            Family.FIFO -> Unit
        }
    }

    private fun release(packet: GamePacket?) {
        val payload = packet?.payload() ?: return
        if (payload.refCnt() > 0) {
            payload.release()
        }
    }

    companion object {
        /** Opcode 122: first-click item action (e.g. bury bones, eat food, drink potion). */
        private const val OPCODE_ITEM_CLICK = 122

        @JvmStatic
        fun familyOf(opcode: Int): Family =
            when (opcode) {
                248, 164, 98 -> Family.WALK
                241 -> Family.MOUSE
                OPCODE_ITEM_CLICK -> Family.ITEM_CLICK
                else -> Family.FIFO
            }
    }
}