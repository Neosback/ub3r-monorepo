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
        assertEquals(53, first.discoveredHandlerCount)
        assertEquals(72, first.registeredCount)
        assertEquals(64, first.fingerprint.length)
        assertTrue(first.missingCriticalOpcodes.isEmpty())
        PacketRegistrationReport.CRITICAL_OPCODES.forEach { opcode ->
            assertTrue(repository.has(opcode), "Missing critical opcode $opcode")
        }

        val countBeforeDispatchLookup = repository.registeredCount
        assertTrue(PacketListenerManager.get(PacketRegistrationReport.CRITICAL_OPCODES.first()) != null)
        assertEquals(countBeforeDispatchLookup, repository.registeredCount)
        assertSame(first, PacketListenerManager.initialize())
        assertSame(PacketListenerManager.get(185), PacketListenerManager.get(186))
        assertSame(PacketListenerManager.get(77), PacketListenerManager.get(229))
        assertEquals(expectedOwnership(), first.opcodeOwners.mapValues { it.value.substringAfterLast('.') })
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

    private fun expectedOwnership(): Map<Int, String> =
        mapOf(
            0 to "KeepAliveListener", 3 to "FocusChangeListener", 4 to "ChatListener",
            11 to "ChangeAppearanceListener", 14 to "UseItemOnPlayerListener", 16 to "ClickItem2Listener",
            17 to "NpcInteractionListener", 18 to "NpcInteractionListener", 21 to "NpcInteractionListener",
            25 to "ItemOnGroundItemListener", 26 to "NpcDropTableListener", 35 to "ObjectInteractionListener",
            39 to "FollowPlayerListener", 40 to "DialogueListener", 41 to "WearItemListener",
            43 to "Bank10Listener", 53 to "ItemOnItemListener", 57 to "UseItemOnNpcListener",
            60 to "SyntaxInputListener", 70 to "ObjectInteractionListener", 72 to "NpcInteractionListener",
            73 to "AttackPlayerListener", 74 to "RemoveIgnoreListener", 75 to "ClickItem3Listener",
            77 to "NoOpPacketListener", 86 to "NoOpPacketListener", 87 to "DropItemListener",
            95 to "UpdateChatListener", 98 to "WalkingListener", 101 to "ChangeAppearanceListener",
            103 to "CommandsListener", 117 to "Bank5Listener", 121 to "ChangeRegionListener",
            122 to "ClickItemListener", 126 to "SendPrivateMessageListener", 128 to "TradeRequestListener",
            129 to "BankAllListener", 130 to "ClickingStuffListener", 131 to "MagicOnNpcListener",
            132 to "ObjectInteractionListener", 133 to "AddIgnoreListener", 135 to "BankX1Listener",
            139 to "TradeListener", 140 to "BankAllButOneListener", 141 to "BankWithdrawRememberedXListener",
            142 to "InputFieldListener", 145 to "RemoveItemListener", 150 to "ExamineListener",
            153 to "DuelRequestListener", 155 to "NpcInteractionListener", 164 to "WalkingListener",
            185 to "ClickingButtonsListener", 186 to "ClickingButtonsListener", 188 to "AddFriendListener",
            192 to "ObjectInteractionListener", 202 to "NoOpPacketListener", 208 to "BankX2Listener",
            210 to "ChangeRegionListener", 214 to "MoveItemsListener", 215 to "RemoveFriendListener",
            216 to "BankTabCreationListener", 228 to "ObjectInteractionListener", 229 to "NoOpPacketListener",
            230 to "NpcInteractionListener", 234 to "ObjectInteractionListener", 236 to "PickUpGroundItemListener",
            237 to "MagicOnItemsListener", 241 to "MouseClicksListener", 248 to "WalkingListener",
            249 to "MagicOnPlayerListener", 252 to "ObjectInteractionListener", 255 to "DropdownMenuListener",
        )
}
