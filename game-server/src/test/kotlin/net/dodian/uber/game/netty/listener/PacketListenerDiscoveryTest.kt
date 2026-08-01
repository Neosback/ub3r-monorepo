package net.dodian.uber.game.netty.listener

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.game.GamePacket
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PacketListenerDiscoveryTest {
    @Test
    fun `discovery is deterministic and sorts opcode bindings`() {
        val first = PacketListenerDiscovery.validateAndInstantiate(listOf(SecondHandler::class.java, FirstHandler::class.java))
        val second = PacketListenerDiscovery.validateAndInstantiate(listOf(FirstHandler::class.java, SecondHandler::class.java))

        assertEquals(listOf(7, 9, 42), first.bindings().map { it.opcode() })
        assertEquals(first.owners(), second.owners())
        assertEquals(first.fingerprint(), second.fingerprint())
    }

    @Test
    fun `missing annotation is rejected`() {
        assertThrows(IllegalStateException::class.java) {
            PacketListenerDiscovery.validateAndInstantiate(listOf(MissingAnnotation::class.java))
        }
    }

    @Test
    fun `duplicate and invalid opcodes are rejected`() {
        assertThrows(IllegalStateException::class.java) {
            PacketListenerDiscovery.validateAndInstantiate(listOf(FirstHandler::class.java, DuplicateHandler::class.java))
        }
        assertThrows(IllegalStateException::class.java) {
            PacketListenerDiscovery.validateAndInstantiate(listOf(InvalidOpcode::class.java))
        }
    }

    @Test
    fun `invalid listener construction and annotation target are rejected`() {
        assertThrows(IllegalStateException::class.java) {
            PacketListenerDiscovery.validateAndInstantiate(listOf(NoDefaultConstructor::class.java))
        }
        assertThrows(IllegalStateException::class.java) {
            PacketListenerDiscovery.validateAndInstantiate(listOf(AnnotatedNonListener::class.java))
        }
    }

    @PacketHandler(opcodes = [42, 7])
    class FirstHandler : PacketListener {
        override fun handle(client: Client, packet: GamePacket) = Unit
    }

    @PacketHandler(opcodes = [9])
    class SecondHandler : PacketListener {
        override fun handle(client: Client, packet: GamePacket) = Unit
    }

    @PacketHandler(opcodes = [42])
    class DuplicateHandler : PacketListener {
        override fun handle(client: Client, packet: GamePacket) = Unit
    }

    @PacketHandler(opcodes = [256])
    class InvalidOpcode : PacketListener {
        override fun handle(client: Client, packet: GamePacket) = Unit
    }

    class MissingAnnotation : PacketListener {
        override fun handle(client: Client, packet: GamePacket) = Unit
    }

    @PacketHandler(opcodes = [10])
    class NoDefaultConstructor(private val value: Int) : PacketListener {
        override fun handle(client: Client, packet: GamePacket) = Unit
    }

    @PacketHandler(opcodes = [11])
    class AnnotatedNonListener
}
