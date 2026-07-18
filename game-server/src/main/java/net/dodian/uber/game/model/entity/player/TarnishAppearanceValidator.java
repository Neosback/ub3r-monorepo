package net.dodian.uber.game.model.entity.player;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.CRC32;
import io.netty.buffer.ByteBuf;

/** Tarnish-specific visual projection and wire validation. Never mutates player state. */
public final class TarnishAppearanceValidator {
    public static final int CLIENT_ITEM_COUNT = 28_790;
    public static final int MAX_APPEARANCE_BYTES = 255;
    private static final int[] DEFAULT_LOOK = {0, 3, 14, 18, 26, 34, 38, 42, 2, 14, 5, 4, 0};
    private static final Set<Integer> MALE_HEAD = Set.of(0,1,2,3,4,5,6,7,8,9,129,130,131,132,133,134,142,144,145,146,147,148,149,150);
    private static final Set<Integer> MALE_BEARD = Set.of(10,11,12,13,14,15,16,17,111,112,113,114,115,116,117);
    private static final Set<Integer> MALE_TORSO = Set.of(18,19,20,21,22,23,24,25,105,106,107,108,109,110);
    private static final Set<Integer> MALE_ARMS = Set.of(26,27,28,29,30,31,32,84,85,86,87,88);
    private static final Set<Integer> MALE_LEGS = Set.of(36,37,38,39,40,41,100,101,102,103,104);
    private static final Set<Integer> FEMALE_HEAD = Set.of(45,46,47,48,49,50,51,52,53,54,55,118,119,120,121,122,123,124,125,126,127,128,141,143);
    private static final Set<Integer> FEMALE_TORSO = Set.of(56,57,58,59,60,89,90,91,92,93,94);
    private static final Set<Integer> FEMALE_ARMS = Set.of(61,62,63,64,65,66,95,96,97,98,99);
    private static final Set<Integer> FEMALE_LEGS = Set.of(70,71,72,73,74,75,76,77,78,135,136,137,138,139,140);

    private TarnishAppearanceValidator() {}

    public static int[] projectLook(int[] saved) {
        if (saved == null || saved.length != 13 || !validLook(saved)) return DEFAULT_LOOK.clone();
        return saved.clone();
    }

    public static int[] projectEquipment(int[] saved) {
        int[] projected = saved == null ? new int[14] : saved.clone();
        for (int slot = 0; slot < projected.length; slot++) {
            int id = projected[slot];
            if (id <= 1) continue;
            if (id >= CLIENT_ITEM_COUNT) projected[slot] = 0;
        }
        return projected;
    }

    public static boolean validLook(int[] look) {
        int gender = look[0];
        if (gender == 0) {
            if (!MALE_HEAD.contains(look[1]) || !MALE_BEARD.contains(look[2]) || !MALE_TORSO.contains(look[3]) ||
                    !MALE_ARMS.contains(look[4]) || look[5] < 33 || look[5] > 34 ||
                    !MALE_LEGS.contains(look[6]) || look[7] < 42 || look[7] > 43) return false;
        } else if (gender == 1) {
            if (!FEMALE_HEAD.contains(look[1]) || look[2] != -1 || !FEMALE_TORSO.contains(look[3]) ||
                    !FEMALE_ARMS.contains(look[4]) || look[5] < 67 || look[5] > 68 ||
                    !FEMALE_LEGS.contains(look[6]) || look[7] < 79 || look[7] > 80) return false;
        } else return false;
        return look[8] >= 0 && look[8] <= 11 && look[9] >= 0 && look[9] <= 15 &&
                look[10] >= 0 && look[10] <= 15 && look[11] >= 0 && look[11] <= 5 &&
                look[12] >= 0 && look[12] <= 9;
    }

    public static Validation validate(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_APPEARANCE_BYTES) {
            return new Validation(false, "length=" + (bytes == null ? -1 : bytes.length), hash(bytes));
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int gender = u8(buffer); int headIcon = u8(buffer); int skullIcon = u8(buffer); int bountyIcon = u8(buffer);
            for (int part = 0; part < 12; part++) {
                int high = u8(buffer);
                if (high == 0) continue;
                int model = (high << 8) | u8(buffer);
                if (part == 0 && model == 0xffff) { u16(buffer); break; }
                if (model >= 512 && model - 512 >= CLIENT_ITEM_COUNT) throw new IllegalArgumentException("item=" + (model - 512));
            }
            for (int i = 0; i < 5; i++) u8(buffer);
            for (int i = 0; i < 7; i++) u16(buffer);
            buffer.getLong();
            for (int i = 0; i < 5; i++) readLine(buffer);
            buffer.getLong(); int rights = u8(buffer); u16(buffer);
            if (buffer.hasRemaining()) throw new IllegalArgumentException("remaining=" + buffer.remaining());
            if (gender > 1 || headIcon > 255 || skullIcon > 255 || bountyIcon != 255 || rights > 16) {
                throw new IllegalArgumentException("icons/rights");
            }
            return new Validation(true, "ok", hash(bytes));
        } catch (RuntimeException exception) {
            return new Validation(false, exception.getMessage(), hash(bytes));
        }
    }

    public static String hash(byte[] bytes) {
        CRC32 crc = new CRC32();
        if (bytes != null) crc.update(bytes);
        return String.format("%08x", crc.getValue());
    }

    public static String hash(ByteBuf buffer) {
        CRC32 crc = new CRC32();
        for (int index = buffer.readerIndex(); index < buffer.writerIndex(); index++) crc.update(buffer.getByte(index));
        return String.format("%08x", crc.getValue());
    }

    private static int u8(ByteBuffer b) { return b.get() & 0xff; }
    private static int u16(ByteBuffer b) { return b.getShort() & 0xffff; }
    private static String readLine(ByteBuffer b) {
        int start = b.position();
        while (b.hasRemaining() && b.get() != 10) {}
        if (!b.hasRemaining() && b.get(b.position() - 1) != 10) throw new IllegalArgumentException("unterminated string");
        return new String(Arrays.copyOfRange(b.array(), start, b.position() - 1), StandardCharsets.ISO_8859_1);
    }

    public static final class Validation {
        public final boolean valid;
        public final String reason;
        public final String hash;
        Validation(boolean valid, String reason, String hash) { this.valid = valid; this.reason = reason; this.hash = hash; }
    }
}
