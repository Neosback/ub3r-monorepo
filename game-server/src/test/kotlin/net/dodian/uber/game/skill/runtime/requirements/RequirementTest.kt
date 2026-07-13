package net.dodian.uber.game.skill.runtime.requirements

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import net.dodian.uber.game.Server
import net.dodian.uber.game.item.ItemManager

class RequirementTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupAll() {
            val coins = net.dodian.uber.game.model.item.Item(
                id = 995,
                name = "Coins",
                slot = -1,
                standAnim = 0,
                walkAnim = 0,
                runAnim = 0,
                attackAnim = 0,
                shopSellValue = 0,
                shopBuyValue = 0,
                bonuses = IntArray(12),
                stackable = true,
                noteable = false,
                tradeable = true,
                twoHanded = false,
                full = false,
                mask = false,
                premium = false,
                examine = "Lovely coins",
                alchemy = 0
            )
            Server.itemManager = ItemManager({ mapOf(995 to coins) }, {})
        }
    }

    private fun testClient(): Client = Client(null, 1)

    @Test
    fun `base level and boosted level requirements check successfully`() {
        val client = testClient()
        client.setExperience(37224, Skill.ATTACK) // base level exactly 40
        client.setLevel(45, Skill.ATTACK) // boosted to 45

        val builder = RequirementBuilder().apply {
            baseLevel(Skill.ATTACK, 40)
            boostedLevel(Skill.ATTACK, 45)
        }
        val reqs = builder.build()
        assertEquals(2, reqs.size)

        assertTrue(reqs[0].validate(client) is ValidationResult.Ok)
        assertTrue(reqs[1].validate(client) is ValidationResult.Ok)
    }

    @Test
    fun `item possession requirements check successfully`() {
        val client = testClient()
        client.addItem(995, 100)
        client.equipment[0] = 3751 // equip hat

        val builder = RequirementBuilder().apply {
            item(995, 100, possession = ItemPossession.INVENTORY)
            item(3751, 1, possession = ItemPossession.EQUIPPED)
            item(995, 1, possession = ItemPossession.ANY)
            item(3751, 1, possession = ItemPossession.ANY)
        }
        val reqs = builder.build()
        assertEquals(4, reqs.size)

        assertTrue(reqs[0].validate(client) is ValidationResult.Ok)
        assertTrue(reqs[1].validate(client) is ValidationResult.Ok)
        assertTrue(reqs[2].validate(client) is ValidationResult.Ok)
        assertTrue(reqs[3].validate(client) is ValidationResult.Ok)
    }
}
