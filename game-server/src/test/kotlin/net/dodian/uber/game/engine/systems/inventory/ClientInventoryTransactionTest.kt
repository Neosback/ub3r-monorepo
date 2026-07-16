package net.dodian.uber.game.engine.systems.inventory

import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.loop.GameThreadContext
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.GameItem
import net.dodian.uber.game.persistence.player.PlayerSaveSegment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class ClientInventoryTransactionTest {
    private var previousItemManager: ItemManager? = null

    @BeforeEach
    fun setUpItemManager() {
        GameThreadContext.bindCurrentThread()
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {})
    }

    @AfterEach
    fun restoreItemManager() {
        GameThreadContext.clearBindingForTests()
        Server.itemManager = previousItemManager
    }

    @Test
    fun `failed transaction leaves live inventory and dirty mask unchanged`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 101
            playerItemsN[0] = 1
        }

        val committed = client.inventoryTransaction {
            remove(100, 1)
            remove(200, 1)
        }

        assertFalse(committed)
        assertEquals(101, client.playerItems[0])
        assertEquals(1, client.playerItemsN[0])
        assertEquals(0, client.saveDirtyMask)
    }

    @Test
    fun `successful replacement commits once and marks inventory dirty`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 101
            playerItemsN[0] = 1
        }

        val committed = client.inventoryTransaction {
            remove(100, 1)
            add(200, 1)
        }

        assertTrue(committed)
        assertEquals(201, client.playerItems[0])
        assertEquals(1, client.playerItemsN[0])
        assertTrue(client.saveDirtyMask and PlayerSaveSegment.INVENTORY.mask != 0)
    }

    @Test
    fun `slot-bound removal rejects a stale item slot`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 101
            playerItemsN[0] = 1
        }

        assertFalse(client.inventoryTransaction { removeAt(0, 200, 1) })
        assertEquals(101, client.playerItems[0])
    }

    @Test
    fun `bank transfer commits inventory and bank together`() {
        val client = Client(null, 1).apply {
            bankItems[4] = 101
            bankItemsN[4] = 3
        }

        assertTrue(EconomyTransaction.transferBankToInventory(client, 100, 4, 2))

        assertEquals(101, client.playerItems[0])
        assertEquals(1, client.playerItemsN[0])
        assertEquals(101, client.playerItems[1])
        assertEquals(1, client.playerItemsN[1])
        assertEquals(101, client.bankItems[4])
        assertEquals(1, client.bankItemsN[4])
        assertTrue(client.saveDirtyMask and PlayerSaveSegment.INVENTORY.mask != 0)
        assertTrue(client.saveDirtyMask and PlayerSaveSegment.BANK.mask != 0)
    }

    @Test
    fun `failed paired trade settlement leaves both inventories and offers intact`() {
        val first = Client(null, 1).apply {
            offeredItems.add(GameItem(100, 1))
        }
        val second = Client(null, 2).apply {
            playerItems.indices.forEach { slot ->
                playerItems[slot] = 200 + slot
                playerItemsN[slot] = 1
            }
            offeredItems.add(GameItem(200, 1))
        }

        assertFalse(EconomyTransaction.settleTrade(first, second))

        assertEquals(1, first.offeredItems.size)
        assertEquals(100, first.offeredItems.single().id)
        assertEquals(1, second.offeredItems.size)
        assertEquals(200, second.offeredItems.single().id)
        assertEquals(200, second.playerItems[0])
        assertEquals(0, first.saveDirtyMask)
        assertEquals(0, second.saveDirtyMask)
    }
}
