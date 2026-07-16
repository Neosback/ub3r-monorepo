package net.dodian.uber.game.netty.listener

import net.dodian.uber.game.engine.systems.net.PacketRegistrationReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PacketListenerManagerTest {
    @Test
    fun `initialization is eager complete locked and idempotent`() {
        val first = PacketListenerManager.initialize()
        val repository = PacketListenerManager.getRepository()

        assertTrue(PacketListenerManager.isInitialized())
        assertTrue(repository.isLocked)
        assertTrue(first.registeredCount > 0)
        assertTrue(first.missingCriticalOpcodes.isEmpty())
        PacketRegistrationReport.CRITICAL_OPCODES.forEach { opcode ->
            assertTrue(repository.has(opcode), "Missing critical opcode $opcode")
        }

        val countBeforeDispatchLookup = repository.registeredCount
        assertTrue(PacketListenerManager.get(PacketRegistrationReport.CRITICAL_OPCODES.first()) != null)
        assertEquals(countBeforeDispatchLookup, repository.registeredCount)
        assertSame(first, PacketListenerManager.initialize())
    }

    @Test
    fun `repository rejects conflicting opcode ownership`() {
        val constructor = PacketRepository::class.java.getDeclaredConstructor().apply { isAccessible = true }
        val repository = constructor.newInstance()
        repository.register(42) { _, _ -> }

        assertThrows(IllegalArgumentException::class.java) {
            repository.register(42) { _, _ -> }
        }
    }
}
