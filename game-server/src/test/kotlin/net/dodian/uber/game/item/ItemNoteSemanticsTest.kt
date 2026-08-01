package net.dodian.uber.game.item

import net.dodian.uber.game.model.item.Item
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ItemNoteSemanticsTest {
    @Test
    fun `explicit notes are stackable and normalize through their declared partner`() {
        val unnoted = item(
            id = 24403,
            json = ItemDefJson(
                id = 24403,
                name = "Twisted boots (t2)",
            ),
        )
        val note = item(
            id = 24454,
            json = ItemDefJson(
                id = 24454,
                name = "Twisted boots (t2)",
                noted = true,
                linkedIdItem = 24403,
            ),
        )
        val manager = manager(unnoted, note)

        assertTrue(manager.isStackable(24454))
        assertTrue(manager.isNote(24454))
        assertEquals(24403, manager.normalizeForBank(24454))
    }

    @Test
    fun `placeholder links are never treated as note links`() {
        val placeholder = item(
            id = 16030,
            json = ItemDefJson(
                id = 16030,
                name = "Rune shield (h5) placeholder",
                placeholder = true,
                linkedIdItem = 7360,
            ),
        )
        val manager = manager(placeholder)

        assertFalse(manager.isNote(16030))
        assertEquals(0, manager.getLinkedItemId(16030))
        assertEquals(16030, manager.normalizeForBank(16030))
    }

    private fun manager(vararg items: Item): ItemManager =
        ItemManager(
            definitionLoader = { items.associateBy { it.id } },
            globalSpawnBootstrap = {},
        )

    private fun item(id: Int, json: ItemDefJson): Item =
        Item.fromDefs(ItemDefBase(id = id, name = json.name), json)
}
