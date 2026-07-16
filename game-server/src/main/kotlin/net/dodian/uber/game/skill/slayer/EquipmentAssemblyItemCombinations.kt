package net.dodian.uber.game.skill.slayer

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.systems.inventory.inventoryTransaction

object EquipmentAssemblyItemCombinations {
    private val slayerHelmItems = intArrayOf(4155, 4156, 4164, 4166, 4168, 4551, 6720, 8923, 11784, 8921)

    @JvmStatic
    fun handle(client: Client, itemUsed: Int, otherItem: Int): Boolean {
        val usedMatches = slayerHelmItems.any { itemUsed == it }
        val otherMatches = slayerHelmItems.any { otherItem == it }
        if (!usedMatches || !otherMatches) {
            return false
        }

        var hasAllItems = true
        for (index in 0 until slayerHelmItems.size - 2) {
            if (!client.playerHasItem(slayerHelmItems[index])) {
                hasAllItems = false
            }
        }
        if (!hasAllItems) {
            client.sendMessage("You need a enchanted gem, mirror shield, face mask, earmuffs, nosepeg, spiny helm,")
            client.sendMessage("slayer gloves, witchwood icon and black mask or black mask (i)")
            return true
        }

        if (!client.playerHasItem(slayerHelmItems[slayerHelmItems.size - 1]) &&
            !client.playerHasItem(slayerHelmItems[slayerHelmItems.size - 2])
        ) {
            return true
        }

        if (client.getSkillLevel(Skill.CRAFTING) < 70) {
            client.sendMessage("You need level 70 crafting to assemble these items together.")
            return true
        }

        val slayerHelm = if (client.playerHasItem(slayerHelmItems[slayerHelmItems.size - 2])) 11865 else 11864
        val assembled = client.inventoryTransaction {
            for (index in 0 until slayerHelmItems.size - 2) remove(slayerHelmItems[index], 1)
            remove(if (slayerHelm == 11865) slayerHelmItems[slayerHelmItems.size - 2] else slayerHelmItems[slayerHelmItems.size - 1], 1)
            add(slayerHelm, 1)
        }
        if (!assembled) return true
        client.sendMessage("You assemble the items together and made a ${client.getItemName(slayerHelm).lowercase()}.")
        return true
    }
}
