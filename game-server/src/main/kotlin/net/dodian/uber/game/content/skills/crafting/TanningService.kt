package net.dodian.uber.game.content.skills.crafting

import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.model.entity.player.Client

object TanningService {
    @JvmStatic
    fun open(client: Client) {
        SkillDataRegistry.craftingTanningHeaderStrings().forEach { definition ->
            client.sendString(definition.text, definition.componentId)
        }

        val labels = SkillDataRegistry.craftingTanningHigherTierLabelIds()
        val higherTier = TanningDefinitions.definitions.filter { it.hideType >= 2 }.sortedBy { it.hideType }
        higherTier.forEachIndexed { index, definition ->
            val base = index * 2
            if (base + 1 >= labels.size) {
                return@forEachIndexed
            }
            client.sendString(definition.displayName, labels[base])
            client.sendString("${formatCoins(definition.coinCost)}gp", labels[base + 1])
        }

        SkillDataRegistry.craftingTanningInterfaceModels().forEach { model ->
            client.sendInterfaceModel(model.componentId, model.zoom, model.itemId)
        }
        client.openInterface(SkillDataRegistry.craftingTanningInterfaceId())
    }

    @JvmStatic
    fun start(client: Client, request: TanningRequest): Boolean {
        val definition = TanningDefinitions.find(request.hideType) ?: return false
        if (!client.playerHasItem(995, definition.coinCost)) {
            client.sendMessage("You need atleast ${definition.coinCost} coins to do this!")
            return true
        }
        var amount = request.amount
        amount = if (client.getInvAmt(995) > amount * definition.coinCost) client.getInvAmt(995) / definition.coinCost else amount
        amount = minOf(amount, client.getInvAmt(definition.hideId))
        repeat(amount.coerceAtLeast(0)) {
            client.deleteItem(definition.hideId, 1)
            client.deleteItem(995, definition.coinCost)
            client.addItem(definition.leatherId, 1)
            client.checkItemUpdate()
        }
        return true
    }

    private fun formatCoins(amount: Int): String = "%,d".format(amount)
}
