package net.dodian.uber.game.engine.net

import io.netty.channel.Channel
import java.util.ArrayDeque
import net.dodian.uber.game.netty.codec.ByteMessage
import org.slf4j.LoggerFactory

/**
 * Per-client queued outbound transport. Messages are appended in call order and
 * drained once per tick. Enforces per-session queue caps and respects Netty
 * writability to prevent unbounded memory growth from slow consumers.
 */
class OutboundSessionQueue {
    private val logger = LoggerFactory.getLogger(OutboundSessionQueue::class.java)

    class DrainResult private constructor(
        private val messageCount: Int,
        private val byteCount: Int,
    ) {
        fun messageCount(): Int = messageCount

        fun byteCount(): Int = byteCount

        companion object {
            private val EMPTY = DrainResult(0, 0)

            @JvmStatic
            fun empty(): DrainResult = EMPTY

            @JvmStatic
            fun of(messageCount: Int, byteCount: Int): DrainResult =
                if (messageCount == 0 && byteCount == 0) {
                    EMPTY
                } else {
                    DrainResult(messageCount, byteCount)
                }
        }
    }

    private val queuedMessages = ArrayDeque<ByteMessage>()
    private var queuedByteCount = 0L

    @Synchronized
    fun enqueue(message: ByteMessage) {
        if (queuedMessages.size >= MAX_QUEUED_MESSAGES) {
            val dropped = queuedMessages.removeFirst()
            queuedByteCount -= maxOf(0, dropped.content().writerIndex().toLong())
            dropped.releaseAll()
            if (droppedCount.incrementAndGet() <= 10) {
                logger.warn(
                    "Outbound queue overflow: dropped oldest message remaining={} bytes={}",
                    queuedMessages.size,
                    queuedByteCount,
                )
            }
        }
        val size = maxOf(0, message.content().writerIndex())
        queuedMessages.addLast(message)
        queuedByteCount += size
    }

    @Synchronized
    fun isEmpty(): Boolean = queuedMessages.isEmpty()

    @Synchronized
    fun drainTo(channel: Channel): DrainResult {
        if (queuedMessages.isEmpty() || !channel.isWritable) {
            return DrainResult.empty()
        }
        var messages = 0
        var bytes = 0
        while (messages < MAX_DRAIN_PER_FLUSH
            && bytes < MAX_DRAIN_BYTES_PER_FLUSH
            && !queuedMessages.isEmpty()
            && channel.isWritable
        ) {
            val message = queuedMessages.removeFirst()
            val size = maxOf(0, message.content().writerIndex())
            bytes += size
            queuedByteCount -= size
            channel.write(message)
            messages++
        }
        if (!queuedMessages.isEmpty() && channel.isWritable) {
            logger.debug(
                "Outbound queue drain partial: drained={} bytes={} remaining={} remainingBytes={}",
                messages,
                bytes,
                queuedMessages.size,
                queuedByteCount,
            )
        }
        return DrainResult.of(messages, bytes)
    }

    @Synchronized
    fun releaseAll() {
        queuedByteCount = 0L
        while (!queuedMessages.isEmpty()) {
            queuedMessages.removeFirst().releaseAll()
        }
    }

    companion object {
        private val droppedCount = java.util.concurrent.atomic.AtomicLong()

        private const val MAX_QUEUED_MESSAGES = 1000
        private const val MAX_DRAIN_PER_FLUSH = 256
        private const val MAX_DRAIN_BYTES_PER_FLUSH = 65536

        @JvmStatic
        fun droppedCount(): Long = droppedCount.get()
    }
}
