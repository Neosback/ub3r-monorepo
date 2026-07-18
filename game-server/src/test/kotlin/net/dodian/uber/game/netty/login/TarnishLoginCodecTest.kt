package net.dodian.uber.game.netty.login

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.channel.embedded.EmbeddedChannel
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TarnishLoginCodecTest {
    @Test
    fun `handshake consumes opcode and name hash then returns seed`() {
        val channel = EmbeddedChannel(LoginHandshakeHandler())

        assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(byteArrayOf(14, 7))))
        val response = channel.readOutbound<ByteBuf>()
        assertEquals(17, response.readableBytes())
        repeat(9) { assertEquals(0, response.readUnsignedByte()) }
        val seed = response.readLong()
        assertEquals(seed, channel.attr(LoginHandshakeHandler.SERVER_SEED_KEY).get())
        assertNotEquals(0L, seed)
        response.release()
        channel.finishAndReleaseAll()
    }

    @Test
    fun `login payload decoder applies Tarnish legacy length correction across fragmentation`() {
        val channel = EmbeddedChannel(LoginPayloadDecoder())
        val wire = byteArrayOf(18, 1, 1, 2, 3)

        wire.forEach { channel.writeInbound(Unpooled.wrappedBuffer(byteArrayOf(it))) }

        val payload = channel.readInbound<LoginPayload>()
        assertTrue(payload.reconnecting())
        val actual = ByteArray(payload.payload().readableBytes())
        payload.payload().readBytes(actual)
        payload.payload().release()
        assertArrayEquals(byteArrayOf(1, 2, 3), actual)
        assertFalse(channel.finish())
    }

    @Test
    fun `login payload decoder does not emit before the two implicit bytes arrive`() {
        val channel = EmbeddedChannel(LoginPayloadDecoder())

        channel.writeInbound(Unpooled.wrappedBuffer(byteArrayOf(16, 1, 9)))
        assertEquals(null, channel.readInbound<LoginPayload>())
        channel.writeInbound(Unpooled.wrappedBuffer(byteArrayOf(8, 7)))

        val payload = channel.readInbound<LoginPayload>()
        assertFalse(payload.reconnecting())
        assertEquals(3, payload.payload().readableBytes())
        payload.payload().release()
        channel.finishAndReleaseAll()
    }

    @Test
    fun `successful login response includes rights and flagged bytes`() {
        val response = LoginProcessorHandler.successResponse(UnpooledByteBufAllocator.DEFAULT, 2)
        val actual = ByteArray(response.readableBytes())
        response.readBytes(actual)
        response.release()
        assertArrayEquals(byteArrayOf(2, 2, 0), actual)
    }
}
