package net.dodian.uber.game.model.entity.player

import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.loop.GameThreadContext
import net.dodian.uber.game.engine.systems.inventory.EconomyTransaction
import net.dodian.uber.game.item.ItemDefBase
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.item.TarnishEquipmentAppearanceType
import net.dodian.uber.game.model.entity.UpdateFlag
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.model.item.Item
import net.dodian.uber.game.persistence.player.PlayerSaveSegment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class EquipmentAppearanceSynchronizationTest {
    private var previousItemManager: ItemManager? = null

    @BeforeEach
    fun setUp() {
        GameThreadContext.bindCurrentThread()
        previousItemManager = Server.itemManager
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {})
    }

    @AfterEach
    fun tearDown() {
        GameThreadContext.clearBindingForTests()
        Server.itemManager = previousItemManager
    }

    @Test
    fun `dynamic appearance overrides resolve correct types`() {
        val baseHelm = ItemDefBase(id = 1153, name = "Iron full helm", equipmentSlot = "head")
        val itemHelm = Item.fromDefs(baseHelm, null, TarnishEquipmentAppearanceType.MASK)
        assertEquals(TarnishEquipmentAppearanceType.HELM, itemHelm.tarnishAppearanceType)

        val baseHood = ItemDefBase(id = 11663, name = "Ahrim's hood", equipmentSlot = "head")
        val itemHood = Item.fromDefs(baseHood, null, TarnishEquipmentAppearanceType.HAT)
        assertEquals(TarnishEquipmentAppearanceType.FACE, itemHood.tarnishAppearanceType)

        val baseMed = ItemDefBase(id = 1137, name = "Iron med helm", equipmentSlot = "head")
        val itemMed = Item.fromDefs(baseMed, null, TarnishEquipmentAppearanceType.HAT)
        assertEquals(TarnishEquipmentAppearanceType.FACE, itemMed.tarnishAppearanceType)
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

    @Test
    fun `appearance bytes match the Tarnish client layout`() {
        val client = Client(null, 1).apply {
            playerName = "appearance-test"
            playerRights = 2
        }

        val bytes = PlayerUpdating.getInstance().getAppearanceBytes(client)

        // Regression for the false-positive "unterminated string" validator bug: the validator
        // previously read 5 lines after the name instead of title + titleColor(int) + 3 lines,
        // which drifted it into the combat-level double and threw on every encode.
        val validation = TarnishAppearanceValidator.validate(bytes)
        assertTrue(validation.valid, "validator reason: ${validation.reason}")

        val buffer = ByteBuffer.wrap(bytes)

        assertEquals(client.gender, buffer.unsignedByte())
        assertEquals(client.headIcon and 0xff, buffer.unsignedByte())
        assertEquals(client.skullIcon and 0xff, buffer.unsignedByte())
        assertEquals(255, buffer.unsignedByte(), "unsupported Tarnish bounty icon must use the absent sentinel")

        for (bodyPart in 0 until 12) {
            val high = buffer.unsignedByte()
            if (high != 0) {
                val model = (high shl 8) or buffer.unsignedByte()
                if (bodyPart == 0 && model == 65_535) {
                    buffer.short
                    break
                }
            }
        }
        repeat(5) { buffer.get() }
        repeat(7) { buffer.short }
        buffer.long // encoded player name

        assertEquals("", buffer.lineString())
        assertEquals(0, buffer.int)
        assertEquals("", buffer.lineString())
        assertEquals("", buffer.lineString())
        assertEquals("", buffer.lineString())
        assertEquals(client.determineCombatLevel().toDouble(), Double.fromBits(buffer.long))
        assertEquals(client.playerRights, buffer.unsignedByte())
        assertEquals(0, buffer.short.toInt() and 0xffff)
        assertEquals(0, buffer.remaining(), "the Tarnish decoder must consume the entire appearance block")
    }

    private fun ByteBuffer.unsignedByte(): Int = get().toInt() and 0xff

    private fun ByteBuffer.lineString(): String {
        val value = StringBuilder()
        while (hasRemaining()) {
            val next = unsignedByte()
            if (next == 10) return value.toString()
            value.append(next.toChar())
        }
        error("unterminated appearance string")
    }
}
