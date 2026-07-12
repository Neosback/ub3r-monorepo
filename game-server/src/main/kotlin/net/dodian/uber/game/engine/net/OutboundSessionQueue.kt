package net.dodian.uber.game.engine.net

import io.netty.channel.Channel
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import net.dodian.uber.game.netty.codec.ByteMessage
import org.jctools.queues.MpscArrayQueue
import org.slf4j.LoggerFactory

/**
 * Per-client queued outbound transport. Messages are appended in call order and
 * drained once per tick. Enforces per-session queue caps and respects Netty
 * writability to prevent unbounded memory growth from slow consumers.
 * Uses JCTools lock-free MpscArrayQueue to eliminate lock overhead.
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

    private val queuedMessages = MpscArrayQueue<ByteMessage>(MAX_QUEUED_MESSAGES)
    private val queueSize = AtomicInteger(0)
    private val queuedByteCount = AtomicLong(0L)

    fun enqueue(message: ByteMessage) {
        val size = maxOf(0, message.content().writerIndex())
        // MpscArrayQueue permits exactly one consumer. Producers must never poll to
        // evict old messages: doing so races the game-thread drain. Drop and release
        // the new message when saturated, preserving queue ownership and ByteBuf refs.
        if (!queuedMessages.offer(message)) {
            message.releaseAll()
            if (droppedCount.incrementAndGet() <= 10) {
                logger.warn(
                    "Outbound queue overflow: rejected newest message remaining={} bytes={}",
                    queueSize.get(),
                    queuedByteCount.get(),
                )
            }
            return
        }
        queueSize.incrementAndGet()
        queuedByteCount.addAndGet(size.toLong())
    }

    fun isEmpty(): Boolean = queuedMessages.isEmpty()

    fun drainTo(channel: Channel): DrainResult {
        if (queuedMessages.isEmpty() || !channel.isWritable) {
            return DrainResult.empty()
        }
        var messages = 0
        var bytes = 0
        while (messages < MAX_DRAIN_PER_FLUSH
            && bytes < MAX_DRAIN_BYTES_PER_FLUSH
            && channel.isWritable
        ) {
            val message = queuedMessages.poll() ?: break
            queueSize.decrementAndGet()
            val size = maxOf(0, message.content().writerIndex())
            bytes += size
            queuedByteCount.addAndGet(-size.toLong())
            channel.write(message)
            messages++
        }
        if (!queuedMessages.isEmpty() && channel.isWritable) {
            logger.debug(
                "Outbound queue drain partial: drained={} bytes={} remaining={} remainingBytes={}",
                messages,
                bytes,
                queueSize.get(),
                queuedByteCount.get(),
            )
        }
        return DrainResult.of(messages, bytes)
    }

    fun releaseAll() {
        queuedByteCount.set(0L)
        queueSize.set(0)
        while (true) {
            val msg = queuedMessages.poll() ?: break
            msg.releaseAll()
        }
    }

    companion object {
        private val droppedCount = AtomicLong()

        private const val MAX_QUEUED_MESSAGES = 1024 // MpscArrayQueue requires power-of-two capacity
        private const val MAX_DRAIN_PER_FLUSH = 256
        private const val MAX_DRAIN_BYTES_PER_FLUSH = 65536

        @JvmStatic
        fun droppedCount(): Long = droppedCount.get()
    }
}
