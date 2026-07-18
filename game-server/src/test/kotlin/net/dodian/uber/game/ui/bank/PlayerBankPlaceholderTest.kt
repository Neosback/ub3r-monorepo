package net.dodian.uber.game.ui.bank

import net.dodian.uber.game.model.entity.player.Client
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlayerBankPlaceholderTest {
    @Test
    fun `placeholder toggle and release preserve ordinary bank entries`() {
        val client = Client(null, 1)
        client.IsBanking = true
        client.bankSlotTabs = IntArray(client.bankSize())
        client.bankItems[2] = 996
        client.bankItemsN[2] = 50_000
        client.bankItems[7] = 4152
        client.bankItemsN[7] = 0
        client.bankSlotTabs[7] = 2

        PlayerBankService.togglePlaceholders(client)
        assertTrue(client.bankPlaceholdersEnabled)
        assertEquals(1, PlayerBankService.releaseAllPlaceholders(client))

        assertEquals(996, client.bankItems[2])
        assertEquals(50_000, client.bankItemsN[2])
        assertEquals(0, client.bankItems[7])
        assertEquals(0, client.bankItemsN[7])
        assertFalse(PlayerBankService.releasePlaceholder(client, 7))
    }
}
