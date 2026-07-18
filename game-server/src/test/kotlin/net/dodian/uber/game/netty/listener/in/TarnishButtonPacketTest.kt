package net.dodian.uber.game.netty.listener.`in`

import io.netty.buffer.Unpooled
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.game.GamePacket
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TarnishButtonPacketTest {
    @Test
    fun `opcode 185 reads a two byte button id`() {
        val client = Client(null, 1)
        client.autoRetaliate = false
        val payload = Unpooled.buffer(2).writeShort(24_041)
        try {
            ClickingButtonsListener().handle(client, GamePacket(185, 2, payload))
            assertTrue(client.autoRetaliate)
            assertFalse(payload.isReadable)
        } finally {
            payload.release()
        }
    }

    @Test
    fun `malformed button payload is rejected without state mutation`() {
        val client = Client(null, 1)
        client.autoRetaliate = false
        val payload = Unpooled.buffer(4).writeInt(24_017)
        try {
            ClickingButtonsListener().handle(client, GamePacket(185, 4, payload))
            assertFalse(client.autoRetaliate)
            assertTrue(payload.isReadable)
        } finally {
            payload.release()
        }
    }
}
