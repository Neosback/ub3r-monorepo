package net.dodian.uber.game.persistence.account.login

import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.loop.GameThreadContext
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.model.item.Item
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccountLoginHydrationTest {
    private var previousItemManager: ItemManager? = null

    @BeforeEach
    fun setUp() {
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {}).also { manager ->
            manager.items[995] = item(995, stackable = true)
            manager.items[1856] = item(1856)
            manager.items[4155] = item(4155)
        }
        GameThreadContext.clearBindingForTests()
        GameThreadContext.resetDiagnosticsForTests()
    }

    @AfterEach
    fun tearDown() {
        GameThreadContext.clearBindingForTests()
        GameThreadContext.resetDiagnosticsForTests()
        Server.itemManager = previousItemManager
    }

    @Test
    fun `new character hydration applies starter state on the game thread`() {
        GameThreadContext.bindCurrentThread()
        val client = Client(null, 1).apply { playerName = "hydration-test" }
        val prepared = AccountLoginService.PreparedLogin(
            code = 0,
            dbId = 42,
            playerGroup = 2,
            newCharacter = true,
        )

        AccountLoginService.hydrateGame(client, prepared)

        assertEquals(42, client.dbId)
        assertEquals(1277, client.equipment[Equipment.Slot.WEAPON.id])
        assertEquals(1171, client.equipment[Equipment.Slot.SHIELD.id])
        assertEquals(2000, inventoryAmount(client, 995))
        assertEquals(1, inventoryAmount(client, 1856))
        assertEquals(1, inventoryAmount(client, 4155))
        assertEquals(0, GameThreadContext.violationCount())
    }

    @Test
    fun `hydration refuses an account worker thread`() {
        val client = Client(null, 1)
        val prepared = AccountLoginService.PreparedLogin(code = 0, dbId = 42, newCharacter = true)
        assertThrows(IllegalStateException::class.java) {
            AccountLoginService.hydrateGame(client, prepared)
        }
    }

    private fun inventoryAmount(client: Client, itemId: Int): Int =
        client.playerItems.indices.sumOf { slot ->
            if (client.playerItems[slot] == itemId + 1) client.playerItemsN[slot] else 0
        }

    private fun item(id: Int, stackable: Boolean = false) = Item(
        id = id,
        name = "item-$id",
        slot = 0,
        standAnim = 808,
        walkAnim = 819,
        runAnim = 824,
        attackAnim = 806,
        shopSellValue = 0,
        shopBuyValue = 0,
        bonuses = IntArray(14),
        stackable = stackable,
        examine = "",
        alchemy = 0,
    )
}
