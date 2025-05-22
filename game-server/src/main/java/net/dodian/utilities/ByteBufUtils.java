package net.dodian.utilities;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class ByteBufUtils {

    public static final int NEWLINE_CHAR = 10; // ASCII value for newline '\n'

    // Prioritized Methods:

    /**
     * Reads a string from the buffer until a newline character (byte 10) is encountered.
     * The newline character is consumed but not included in the returned string.
     *
     * @param buf The buffer to read from.
     * @return The string read.
     */
    public static String readString(ByteBuf buf) {
        int length = buf.bytesBefore((byte) NEWLINE_CHAR);
        if (length == -1) {
            // Or handle as an error, or read all available bytes if that's preferred.
            // For now, assume if no newline, it might be an incomplete string or an error.
            // Reading all readable bytes as a fallback, but this might not be desired.
            // Consider if a maximum string length should be enforced.
            length = buf.readableBytes();
        }
        String value = buf.readCharSequence(length, StandardCharsets.ISO_8859_1).toString(); // Or UTF_8 depending on client
        if (buf.isReadable() && buf.getByte(buf.readerIndex()) == NEWLINE_CHAR) {
            buf.skipBytes(1); // Consume the newline character
        }
        return value;
    }

    /**
     * Writes a string to the buffer and appends a newline character (byte 10).
     *
     * @param buf The buffer to write to.
     * @param s   The string to write.
     */
    public static void writeString(ByteBuf buf, String s) {
        buf.writeCharSequence(s, StandardCharsets.ISO_8859_1); // Or UTF_8
        buf.writeByte(NEWLINE_CHAR);
    }

    /**
     * Reads an 8-byte long value from the buffer.
     *
     * @param buf The buffer to read from.
     * @return The long value read.
     */
    public static long readLong(ByteBuf buf) {
        return buf.readLong();
    }

    /**
     * Writes an 8-byte long value to the buffer.
     *
     * @param buf   The buffer to write to.
     * @param value The long value to write.
     */
    public static void writeLong(ByteBuf buf, long value) {
        buf.writeLong(value);
    }

    /**
     * Reads a 4-byte integer value from the buffer.
     *
     * @param buf The buffer to read from.
     * @return The integer value read.
     */
    public static int readInt(ByteBuf buf) {
        return buf.readInt();
    }

    /**
     * Writes a 4-byte integer value to the buffer.
     *
     * @param buf   The buffer to write to.
     * @param value The integer value to write.
     */
    public static void writeInt(ByteBuf buf, int value) {
        buf.writeInt(value);
    }

    /**
     * Writes a 2-byte short value to the buffer in big-endian order.
     *
     * @param buf   The buffer to write to.
     * @param value The short value to write.
     */
    public static void writeWordBigEndian(ByteBuf buf, int value) {
        buf.writeShortBE(value);
    }

    /**
     * Reads an unsigned byte (0-255) from the buffer.
     *
     * @param buf The buffer to read from.
     * @return The unsigned byte value as an int.
     */
    public static int readUnsignedByte(ByteBuf buf) {
        return buf.readUnsignedByte();
    }

    /**
     * Writes a single byte to the buffer.
     *
     * @param buf   The buffer to write to.
     * @param value The byte value to write (int is used for convenience, only lowest 8 bits are written).
     */
    public static void writeByte(ByteBuf buf, int value) {
        buf.writeByte(value);
    }

    // Other Requested Methods:

    /**
     * Reads a signed byte (-128 to 127) from the buffer.
     *
     * @param buf The buffer to read from.
     * @return The signed byte value.
     */
    public static byte readSignedByte(ByteBuf buf) {
        return buf.readByte();
    }

    /**
     * Reads a 2-byte signed short value from the buffer (little-endian by default in Netty).
     *
     * @param buf The buffer to read from.
     * @return The signed short value.
     */
    public static short readSignedWord(ByteBuf buf) {
        return buf.readShort();
    }

    /**
     * Reads a 2-byte unsigned short value (0-65535) from the buffer (little-endian by default in Netty).
     *
     * @param buf The buffer to read from.
     * @return The unsigned short value as an int.
     */
    public static int readUnsignedWord(ByteBuf buf) {
        return buf.readUnsignedShort();
    }
    
    /**
     * Writes a 2-byte short value to the buffer (little-endian by default in Netty).
     *
     * @param buf   The buffer to write to.
     * @param value The short value to write.
     */
    public static void writeWord(ByteBuf buf, int value) {
        buf.writeShort(value);
    }
}
