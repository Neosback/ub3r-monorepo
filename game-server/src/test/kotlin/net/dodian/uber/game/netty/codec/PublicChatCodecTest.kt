package net.dodian.uber.game.netty.codec

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.persistence.audit.ConsoleAuditLog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PublicChatCodecTest {
    @Test
    fun `decodes transformed and reversed public chat payload`() {
        val decoded = PublicChatCodec.decode(clientPayload(effects = 3, color = 7, message = "hello! how are you?"))

        requireNotNull(decoded)
        assertEquals(7, decoded.color)
        assertEquals(3, decoded.effects)
        assertEquals("Hello! How are you?", decoded.message)
    }

    @Test
    fun `decodes hi exactly as the client displays and audits it`() {
        val decoded = requireNotNull(PublicChatCodec.decode(clientPayload(effects = 0, color = 0, message = "hi")))
        val player = Client(null, 1).apply {
            playerName = "Admin"
            dbId = 1
        }

        assertEquals("Hi", decoded.message)
        assertEquals(
            "PUBLIC CHAT | player=Admin | dbId=1 | msg=\"Hi\"",
            ConsoleAuditLog.chatAuditText("PUBLIC", player, decoded.message, null),
        )
    }

    @Test
    fun `rejects invalid public chat character indexes`() {
        assertNull(PublicChatCodec.decode(byteArrayOf(128.toByte(), 128.toByte(), 196.toByte())))
    }

    @Test
    fun `encodes Tarnish text indexes for outbound player updates`() {
        assertEquals(listOf(6, 1, 11, 11, 4, 38), PublicChatCodec.encode("Hello!").map { it.toInt() and 0xff })
    }

    private fun clientPayload(effects: Int, color: Int, message: String): ByteArray {
        val encoded = message.lowercase().map { character ->
            characterTable.indexOf(character).also { require(it >= 0) }
        }
        return ByteArray(encoded.size + 2).also { payload ->
            payload[0] = (128 - effects).toByte()
            payload[1] = (128 - color).toByte()
            for (index in encoded.indices) {
                payload[index + 2] = (encoded[encoded.lastIndex - index] + 128).toByte()
            }
        }
    }

    private companion object {
        val characterTable =
            charArrayOf(
                ' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r', 'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p',
                'b', 'v', 'k', 'x', 'j', 'q', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '!', '?',
                '.', ',', ':', ';', '(', ')', '-', '&', '*', '\\', '\'', '@', '#', '+', '=', '\u00a3', '$', '%', '"',
                '[', ']', '_', '/', '<', '>', '^', '|',
            )
    }
}
