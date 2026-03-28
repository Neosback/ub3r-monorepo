package net.dodian.uber.game.content.skills.crafting

import net.dodian.uber.game.model.entity.player.Client

object TanningService {
    @JvmStatic
    fun open(client: Client) {
        client.sendString("Regular Leather", 14777)
        client.sendString("50gp", 14785)
        client.sendString("", 14781)
        client.sendString("", 14789)
        client.sendString("", 14778)
        client.sendString("", 14786)
        client.sendString("", 14782)
        client.sendString("", 14790)

        val labels = intArrayOf(14779, 14787, 14783, 14791, 14780, 14788, 14784, 14792)
        val higherTier = TanningDefinitions.definitions.filter { it.hideType >= 2 }.sortedBy { it.hideType }
        higherTier.forEachIndexed { index, definition ->
            val base = index * 2
            if (base + 1 >= labels.size) {
                return@forEachIndexed
            }
            client.sendString(definition.displayName, labels[base])
            client.sendString("${formatCoins(definition.coinCost)}gp", labels[base + 1])
        }

        client.sendInterfaceModel(14769, 250, 1741)
        client.sendInterfaceModel(14773, 250, -1)
        client.sendInterfaceModel(14771, 250, 1753)
        client.sendInterfaceModel(14772, 250, 1751)
        client.sendInterfaceModel(14775, 250, 1749)
        client.sendInterfaceModel(14776, 250, 1747)
        client.openInterface(14670)
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
