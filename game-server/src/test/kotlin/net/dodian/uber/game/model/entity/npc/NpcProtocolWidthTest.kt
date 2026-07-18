package net.dodian.uber.game.model.entity.npc

import net.dodian.uber.game.netty.codec.ByteMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NpcProtocolWidthTest {
    @Test
    fun `definition ids use all sixteen bits`() {
        for (id in listOf(16_383, 16_384, 50_046, 65_535)) {
            NpcUpdating.validateNpcDefinitionId(id)
            val message = ByteMessage.raw()
            try {
                message.startBitAccess()
                message.putBits(NpcUpdating.NPC_DEFINITION_BITS, id)
                message.endBitAccess()
                assertEquals(id, message.buffer.readUnsignedShort())
            } finally {
                message.release()
            }
        }
    }

    @Test
    fun `definition ids outside sixteen bits are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { NpcUpdating.validateNpcDefinitionId(-1) }
        assertThrows(IllegalArgumentException::class.java) { NpcUpdating.validateNpcDefinitionId(65_536) }
    }

    @Test
    fun `slots use Tarnish sixteen bit framing while respecting the client array`() {
        assertEquals(16, NpcUpdating.NPC_SLOT_BITS)
        assertEquals(65_535, NpcUpdating.NPC_SLOT_TERMINATOR)
        assertEquals(16_383, NpcUpdating.MAX_CLIENT_NPC_SLOT)
        NpcUpdating.validateNpcSlot(16_383)
        assertThrows(IllegalArgumentException::class.java) { NpcUpdating.validateNpcSlot(16_384) }
    }
}
