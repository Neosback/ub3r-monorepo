package net.dodian.uber.game.model.entity.player

import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.systems.inventory.EconomyTransaction
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.UpdateFlag
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.persistence.player.PlayerSaveSegment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EquipmentAppearanceSynchronizationTest {
    private var previousItemManager: ItemManager? = null

    @BeforeEach
    fun setUp() {
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {})
    }

    @AfterEach
    fun tearDown() {
        Server.itemManager = previousItemManager
    }

    @Test
    fun `raw equipment changes cannot reuse an older appearance cache`() {
        val client = Client(null, 1).apply { playerName = "appearance-test" }
        client.equipment[Equipment.Slot.WEAPON.id] = 1277
        val firstAppearance = PlayerUpdating.getInstance().getAppearanceBytes(client)
        assertTrue(client.isCachedAppearanceValid())

        client.equipment[Equipment.Slot.WEAPON.id] = 1205

        assertFalse(client.isCachedAppearanceValid())
        assertNotEquals(firstAppearance.toList(), PlayerUpdating.getInstance().getAppearanceBytes(client).toList())
    }

    @Test
    fun `equipment transaction invalidates appearance and marks equipment persistence`() {
        val client = Client(null, 1).apply {
            playerName = "appearance-test"
            equipment[Equipment.Slot.WEAPON.id] = 1277
            equipmentN[Equipment.Slot.WEAPON.id] = 1
        }
        PlayerUpdating.getInstance().getAppearanceBytes(client)

        val committed = EconomyTransaction.run {
            equipment(client).removeAt(Equipment.Slot.WEAPON.id, 1277, 1)
            equipment(client).add(1205, 1)
        }

        assertTrue(committed)
        assertTrue(client.saveDirtyMask and PlayerSaveSegment.EQUIPMENT.mask != 0)
        assertTrue(client.updateFlags.isRequired(UpdateFlag.APPEARANCE))
        assertFalse(client.isCachedAppearanceValid())
    }
}
