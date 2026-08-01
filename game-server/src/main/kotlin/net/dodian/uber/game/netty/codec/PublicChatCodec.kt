package net.dodian.uber.game.netty.codec

/**
 * Decodes opcode-4 public chat exactly as the active client writes it.
 *
 * The first two bytes use the subtract transform and the character indexes
 * are written in reverse order with the add transform. Public chat indexes
 * are not the packed-nibble format used by private messages.
 */
object PublicChatCodec {
    private const val MAX_MESSAGE_LENGTH = 100

    private val characters =
        charArrayOf(
            ' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r', 'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p',
            'b', 'v', 'k', 'x', 'j', 'q', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '!', '?',
            '.', ',', ':', ';', '(', ')', '-', '&', '*', '\\', '\'', '@', '#', '+', '=', '\u00a3', '$', '%', '"',
            '[', ']', '_', '/', '<', '>', '^', '|',
        )

    @JvmStatic
    fun decode(payload: ByteArray): DecodedPublicChat? {
        if (payload.size !in 3..(MAX_MESSAGE_LENGTH + 2)) {
            return null
        }

        val effects = subtractTransform(payload[0])
        val color = subtractTransform(payload[1])
        val messageLength = payload.size - 2
        val decoded = CharArray(messageLength)

        for (wireIndex in 0 until messageLength) {
            val characterIndex = addTransform(payload[wireIndex + 2])
            val character = characters.getOrNull(characterIndex) ?: return null
            decoded[messageLength - 1 - wireIndex] = character
        }

        return DecodedPublicChat(color = color, effects = effects, message = formatSentence(decoded))
    }

    /** Encodes text into the character indexes consumed by Tarnish TextInput. */
    @JvmStatic
    fun encode(message: String): ByteArray =
        message.lowercase().take(MAX_MESSAGE_LENGTH).map { character ->
            val index = characters.indexOf(character).takeIf { it >= 0 } ?: 0
            index.toByte()
        }.toByteArray()

    private fun subtractTransform(value: Byte): Int = (128 - (value.toInt() and 0xff)) and 0xff

    private fun addTransform(value: Byte): Int = ((value.toInt() and 0xff) - 128) and 0xff

    private fun formatSentence(message: CharArray): String {
        var capitalizeNext = true
        for (index in message.indices) {
            val character = message[index]
            when {
                character == '.' || character == '!' || character == '?' -> capitalizeNext = true
                capitalizeNext && !character.isWhitespace() -> {
                    message[index] = character.uppercaseChar()
                    capitalizeNext = false
                }

                else -> message[index] = character.lowercaseChar()
            }
        }
        return String(message)
    }
}

data class DecodedPublicChat(
    val color: Int,
    val effects: Int,
    val message: String,
)
