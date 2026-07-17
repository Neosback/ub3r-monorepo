package net.dodian.uber.game.skill.herblore

import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.npc.HerbloreNpcDialogue
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.model.player.skills.Skills
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces
import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.engine.systems.action.PolicyPreset

object Herblore {
    @JvmStatic
    fun start(client: Client, request: HerbloreBatchRequest) = processBatch(client, request)

    @JvmStatic
    fun attempt(client: Client, amount: Int): Boolean = handleEnteredAmount(client, amount)

    @JvmStatic
    fun handleEnteredAmount(client: Client, enteredAmount: Int): Boolean {
        if (client.XinterfaceID != HerbloreData.BATCH_INTERFACE_ID) {
            return false
        }
        client.send(RemoveInterfaces())
        val slot = client.XremoveSlot - 1
        if (slot !in client.herbOptions.indices) {
            client.XinterfaceID = -1
            return true
        }
        val legacyNpcId = DialogueService.activeNpcId(client)
        val request = HerbloreBatchRequest(
            slot = slot,
            amount = enteredAmount,
            npcId = if (legacyNpcId > 0) legacyNpcId else HerbloreData.BATCH_INTERFACE_ID,
        )
        processBatch(client, request)
        client.XinterfaceID = -1
        return true
    }

    @JvmStatic
    fun processBatch(client: Client, request: HerbloreBatchRequest) {
        var amount = request.amount
        val id = client.herbOptions[request.slot].getId()
        val grimy = client.getItemName(id).lowercase().contains("grimy")
        val coins = client.getInvAmt(995)

        if (grimy) {
            amount = minOf(amount, coins / HerbloreData.GRIND_COST_PER_HERB, client.getInvAmt(id))
            if (amount > 0) {
                if (client.getInvAmt(id) <= amount) {
                    client.herbOptions.removeAt(request.slot)
                }
                val otherHerb =
                    HerbloreData.herbDefinitions
                        .firstOrNull { id == client.getNotedItem(it.grimyId) }
                        ?.let { client.getNotedItem(it.cleanId) }
                        ?: -1
                client.deleteItem(995, amount * HerbloreData.GRIND_COST_PER_HERB)
                client.deleteItem(id, amount)
                client.addItem(otherHerb, amount)
                client.checkItemUpdate()
                HerbloreNpcDialogue.showBatchResultAndContinue(client, request.npcId, "Here is your all of ", "$amount ${client.getItemName(id).lowercase()}")
            } else {
                DialogueService.showNpcChat(client, request.npcId, 605, arrayOf("You need 1 herb and 200 coins", "for me to grind it for you."))
            }
            return
        }

        if (!client.playerHasItem(HerbloreData.VIAL_OF_WATER_ID)) {
            DialogueService.showNpcChat(client, request.npcId, 605, arrayOf("You need noted vial of water for me to do that!"))
            return
        }

        val otherHerb =
            HerbloreData.herbDefinitions
                .firstOrNull { id == client.getNotedItem(it.unfinishedPotionId) }
                ?.let { client.getNotedItem(it.cleanId) }
                ?: -1
        val vials = client.getInvAmt(HerbloreData.VIAL_OF_WATER_ID)
        val herbs = client.getInvAmt(otherHerb)
        amount = minOf(amount, coins / HerbloreData.UNFINISHED_POTION_COST, vials, herbs)
        if (amount > 0) {
            if (herbs <= amount) {
                client.herbOptions.removeAt(request.slot)
            }
            client.deleteItem(995, amount * HerbloreData.UNFINISHED_POTION_COST)
            client.deleteItem(HerbloreData.VIAL_OF_WATER_ID, amount)
            client.deleteItem(otherHerb, amount)
            client.addItem(id, amount)
            client.checkItemUpdate()
            HerbloreNpcDialogue.showBatchResultAndContinue(client, request.npcId, "Here is your all of ", "$amount ${client.getItemName(id).lowercase()}")
        } else {
            DialogueService.showNpcChat(
                client,
                request.npcId,
                605,
                arrayOf(
                    "You need atleast 1 herb, 1 vial of water and 1000 coins",
                    "for me to turn it into a unfinish potion.",
                ),
            )
        }
    }
}

object HerbloreSkillPlugin : SkillPlugin {
    override val definition =
        skillPlugin(name = "Herblore", skill = Skill.HERBLORE) {
            val grimyHerbIds = HerbloreData.herbDefinitions.map { it.grimyId }.toIntArray()
            itemClick(preset = PolicyPreset.PRODUCTION, option = 1, *grimyHerbIds) { interaction ->
                val client = net.dodian.uber.game.engine.systems.skills.SkillEngineAccess.client(interaction.player)
                val itemId = interaction.itemId
                val itemSlot = interaction.itemSlot
                val interfaceId = interaction.interfaceId
                val herb = HerbloreData.findHerbDefinitionByGrimy(itemId)
                if (herb != null) {
                    val herbloreLevel = Skills.getLevelForExperience(client.getExperience(Skill.HERBLORE))
                    val requiredLevel = herb.requiredLevel
                    if (herbloreLevel < requiredLevel) {
                        client.sendMessage("You need level $requiredLevel herblore to clean this herb.")
                    } else {
                        ProgressionService.addXp(client, herb.cleaningExperience, Skill.HERBLORE)
                        client.deleteItem(itemId, itemSlot, 1)
                        client.addItemSlot(herb.cleanId, 1, itemSlot)
                        client.sendMessage("You clean the ${client.getItemName(itemId)}.")
                    }
                    true
                } else false
            }

            val supplyIds = intArrayOf(11877, 11879, 12859)
            itemClick(preset = PolicyPreset.PRODUCTION, option = 1, *supplyIds) { interaction ->
                val client = net.dodian.uber.game.engine.systems.skills.SkillEngineAccess.client(interaction.player)
                val itemId = interaction.itemId
                val itemSlot = interaction.itemSlot
                val interfaceId = interaction.interfaceId
                when (itemId) {
                    11877 -> {
                        client.deleteItem(11877, itemSlot, 1)
                        if (!client.playerHasItem(230)) {
                            client.addItemSlot(230, 100, itemSlot)
                        } else {
                            client.addItem(230, 100)
                        }
                        true
                    }
                    11879 -> {
                        client.deleteItem(11879, itemSlot, 1)
                        if (!client.playerHasItem(228)) {
                            client.addItemSlot(228, 100, itemSlot)
                        } else {
                            client.addItem(228, 100)
                        }
                        true
                    }
                    12859 -> {
                        client.deleteItem(12859, itemSlot, 1)
                        if (!client.playerHasItem(222)) {
                            client.addItemSlot(222, 100, itemSlot)
                        } else {
                            client.addItem(222, 100)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
}
