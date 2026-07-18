package net.dodian.uber.game.model.entity.player

import io.netty.channel.embedded.EmbeddedChannel
import net.dodian.uber.game.netty.codec.ByteMessage
import net.dodian.uber.game.netty.listener.out.TarnishItemContainerEncoder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TarnishStartupVisualStateTest {
    @Test
    fun `brightness varp precedes the first item container snapshot`() {
        val channel = EmbeddedChannel()
        val client = Client(channel, 1)

        PlayerInitializer.sendTarnishVisualDefaults(client)
        client.send(TarnishItemContainerEncoder.full(3214, intArrayOf(995), intArrayOf(2_000)))

        val brightness = channel.readOutbound<ByteMessage>()
        val inventory = channel.readOutbound<ByteMessage>()
        try {
            assertEquals(36, brightness.opcode)
            assertEquals(PlayerInitializer.TARNISH_BRIGHTNESS_VARP, brightness.buffer.readUnsignedShortLE())
            assertEquals(PlayerInitializer.DEFAULT_TARNISH_BRIGHTNESS, brightness.buffer.readByte().toInt())
            assertEquals(0, brightness.buffer.readableBytes())

            assertEquals(53, inventory.opcode)
            assertEquals(3214, inventory.buffer.readInt())
        } finally {
            brightness.releaseAll()
            inventory.releaseAll()
            channel.finishAndReleaseAll()
        }
    }
}
