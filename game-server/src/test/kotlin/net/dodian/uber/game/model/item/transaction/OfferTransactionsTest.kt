package net.dodian.uber.game.model.item.transaction

import net.dodian.uber.game.Server
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.GameItem
import net.dodian.uber.game.model.item.Item
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OfferTransactionsTest {
    private var previousItemManager: ItemManager? = null

    @BeforeEach
    fun setUpItemManager() {
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(
            definitionLoader = {
                mapOf(
                    100 to item(100, stackable = false),
                    200 to item(200, stackable = false),
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
    fun `staking then unstaking an item returns it to the same inventory state`() {
        val client = Client(null, 1).apply {
            playerItems[0] = 101
            playerItemsN[0] = 1
        }

        assertTrue(OfferTransactions.stakeToOffer(client, 100, fromSlot = 0, amount = 1))
        assertEquals(0, client.playerItems[0])
        assertEquals(1, client.offeredItems.size)
        assertEquals(100, client.offeredItems[0].id)

        assertTrue(OfferTransactions.offerToInventory(client, 100, fromSlot = 0, amount = 1))
        assertEquals(101, client.playerItems[0])
        assertTrue(client.offeredItems.isEmpty())
    }

    @Test
    fun `settleTrade moves both offers into each other's inventory atomically`() {
        val first = Client(null, 1).apply { offeredItems.add(GameItem(100, 1)) }
        val second = Client(null, 2).apply { offeredItems.add(GameItem(200, 1)) }

        assertTrue(OfferTransactions.settleTrade(first, second))

        assertEquals(201, first.playerItems[0])
        assertEquals(101, second.playerItems[0])
        assertTrue(first.offeredItems.isEmpty())
        assertTrue(second.offeredItems.isEmpty())
    }

    @Test
    fun `settleTrade touches neither player when one inventory is full`() {
        val first = Client(null, 1).apply { offeredItems.add(GameItem(100, 1)) }
        val second = Client(null, 2).apply {
            playerItems.indices.forEach { slot ->
                playerItems[slot] = 300 + slot
                playerItemsN[slot] = 1
            }
            offeredItems.add(GameItem(200, 1))
        }
        val secondInventoryBefore = second.playerItems.copyOf()

        assertFalse(OfferTransactions.settleTrade(first, second))

        assertEquals(1, first.offeredItems.size)
        assertEquals(100, first.offeredItems.single().id)
        assertEquals(1, second.offeredItems.size)
        assertEquals(200, second.offeredItems.single().id)
        assertTrue(second.playerItems.contentEquals(secondInventoryBefore))
        assertTrue(first.playerItems.all { it == 0 })
    }

    @Test
    fun `refundOffers returns a single client's stake to their inventory`() {
        val client = Client(null, 1).apply { offeredItems.add(GameItem(100, 1)) }

        assertTrue(OfferTransactions.refundOffers(client))

        assertEquals(101, client.playerItems[0])
        assertTrue(client.offeredItems.isEmpty())
    }

    @Test
    fun `refundOffers restores both clients in one commit`() {
        val first = Client(null, 1).apply { offeredItems.add(GameItem(100, 1)) }
        val second = Client(null, 2).apply { offeredItems.add(GameItem(200, 1)) }

        assertTrue(OfferTransactions.refundOffers(first, second))

        assertEquals(101, first.playerItems[0])
        assertEquals(201, second.playerItems[0])
        assertTrue(first.offeredItems.isEmpty())
        assertTrue(second.offeredItems.isEmpty())
    }

    @Test
    fun `settleDuelPayout moves both stakes into the winner's inventory`() {
        val winner = Client(null, 1).apply { offeredItems.add(GameItem(100, 1)) }
        val loser = Client(null, 2).apply { offeredItems.add(GameItem(200, 1)) }

        assertTrue(OfferTransactions.settleDuelPayout(winner, loser))

        assertEquals(101, winner.playerItems[0])
        assertEquals(201, winner.playerItems[1])
        assertTrue(winner.offeredItems.isEmpty())
        assertTrue(loser.offeredItems.isEmpty())
    }

    @Test
    fun `settleDuelPayout fails safely when the winner's inventory is full`() {
        val winner = Client(null, 1).apply {
            playerItems.indices.forEach { slot ->
                playerItems[slot] = 300 + slot
                playerItemsN[slot] = 1
            }
            offeredItems.add(GameItem(100, 1))
        }
        val loser = Client(null, 2).apply { offeredItems.add(GameItem(200, 1)) }

        assertFalse(OfferTransactions.settleDuelPayout(winner, loser))

        assertEquals(1, winner.offeredItems.size)
        assertEquals(100, winner.offeredItems.single().id)
        assertEquals(1, loser.offeredItems.size)
        assertEquals(200, loser.offeredItems.single().id)
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
