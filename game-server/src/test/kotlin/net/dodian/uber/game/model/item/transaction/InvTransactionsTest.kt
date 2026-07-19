package net.dodian.uber.game.model.item.transaction

import net.dodian.uber.game.Server
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Item
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InvTransactionsTest {
    private var previousItemManager: ItemManager? = null

    @BeforeEach
    fun setUpItemManager() {
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(
            definitionLoader = {
                mapOf(
                    100 to item(100, stackable = false),
                    995 to item(995, stackable = true),
                )
            },
            globalSpawnBootstrap = {},
        )
    }

    @AfterEach
    fun restoreItemManager() {
        Server.itemManager = previousItemManager
    }

    @Test
    fun `insert adds a new inventory stack`() {
        val client = Client(null, 1)

        val committed = client.invAdd(995, 50)

        assertTrue(committed)
        assertEquals(996, client.playerItems[0])
        assertEquals(50, client.playerItemsN[0])
    }

    @Test
    fun `delete removes exactly the requested amount`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 996
            playerItemsN[0] = 50
        }

        val committed = client.invDel(995, 20)

        assertTrue(committed)
        assertEquals(996, client.playerItems[0])
        assertEquals(30, client.playerItemsN[0])
    }

    @Test
    fun `delete more than available fails and leaves inventory untouched`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 996
            playerItemsN[0] = 5
        }

        val committed = client.invDel(995, 20)

        assertFalse(committed)
        assertEquals(996, client.playerItems[0])
        assertEquals(5, client.playerItemsN[0])
    }

    @Test
    fun `insert into a full non-stackable inventory fails without any partial commit`() {
        val client = Client(null, 1).apply {
            playerItems.indices.forEach { slot ->
                playerItems[slot] = 101
                playerItemsN[slot] = 1
            }
        }

        val committed = client.invAdd(100, 1)

        assertFalse(committed)
        assertTrue(client.playerItems.all { it == 101 })
    }

    @Test
    fun `deposit and withdraw round trip through the bank`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 996
            playerItemsN[0] = 50
        }

        assertTrue(client.invDeposit(fromSlot = 0, amount = 30))
        assertEquals(20, client.playerItemsN[0])
        assertEquals(996, client.bankItems[0])
        assertEquals(30, client.bankItemsN[0])

        assertTrue(client.invWithdraw(bankSlot = 0, amount = 10))
        assertEquals(30, client.playerItemsN[0])
        assertEquals(20, client.bankItemsN[0])
    }

    @Test
    fun `a query that throws mid-block rolls the whole transaction back`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 996
            playerItemsN[0] = 10
        }

        val committed = client.invTransaction { inv ->
            delete { from = inv.transactionInv; obj = 995; strictCount = 5 }
            // This second query requests more than remains and must throw, discarding
            // the first query's in-memory mutation along with it.
            delete { from = inv.transactionInv; obj = 995; strictCount = 100 }
        }

        assertFalse(committed)
        assertEquals(996, client.playerItems[0])
        assertEquals(10, client.playerItemsN[0])
    }

    private fun item(id: Int, stackable: Boolean) = Item(
        id = id,
        name = "test-$id",
        slot = -1,
        standAnim = 0,
        walkAnim = 0,
        runAnim = 0,
        attackAnim = 0,
        shopSellValue = 0,
        shopBuyValue = 0,
        bonuses = IntArray(12),
        stackable = stackable,
        noteable = false,
        tradeable = true,
        twoHanded = false,
        full = false,
        mask = false,
        premium = false,
        examine = "test",
        alchemy = 0,
    )
}
