package net.dodian.utilities

import java.security.MessageDigest
import org.slf4j.LoggerFactory

class MD5(
    private val inStr: String,
) {
    private val md5: MessageDigest? =
        try {
            MessageDigest.getInstance("MD5")
        } catch (exception: Exception) {
            logger.error("Legacy MD5 provider is unavailable; login compatibility hashing cannot proceed", exception)
            null
        }

    fun compute(): String {
        val charArray = inStr.toCharArray()
        val byteArray = ByteArray(charArray.size)
        for (i in charArray.indices) {
            byteArray[i] = charArray[i].code.toByte()
        }
        val md5Bytes = md5?.digest(byteArray) ?: return ""
        val hexValue = StringBuilder()
        for (i in md5Bytes.indices) {
            val value = md5Bytes[i].toInt() and 0xFF
            if (value < 16) {
                hexValue.append('0')
            }
            hexValue.append(Integer.toHexString(value))
        }
        return hexValue.toString()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MD5::class.java)
        @JvmStatic
        fun main(args: Array<String>) {
            println(MD5(args[0]).compute())
        }
    }
}
