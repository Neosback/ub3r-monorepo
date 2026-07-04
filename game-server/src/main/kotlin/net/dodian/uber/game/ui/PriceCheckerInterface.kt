package net.dodian.uber.game.ui

import net.dodian.uber.game.ui.buttons.InterfaceButtonContent
import net.dodian.uber.game.ui.buttons.buttonBinding
import net.dodian.uber.game.netty.listener.out.SendEnterName

object PriceCheckerInterface : InterfaceButtonContent {
    private val buttonAddAll = intArrayOf(48505)
    private val buttonWithdrawAll = intArrayOf(48578)
    private val buttonSearch = intArrayOf(48508)
    private val buttonPriceValue = intArrayOf(48584)
    private val buttonPriceAlch = intArrayOf(48585)

    override val bindings =
        listOf(
            buttonBinding(-1, 0, "price_checker.add_all", buttonAddAll) { client, _ ->
                if (client.priceCheckerOpen) {
                    client.priceCheckerDepositAll()
                }
                true
            },
            buttonBinding(-1, 1, "price_checker.withdraw_all", buttonWithdrawAll) { client, _ ->
                if (client.priceCheckerOpen) {
                    client.priceCheckerWithdrawAll()
                }
                true
            },
            buttonBinding(-1, 2, "price_checker.search", buttonSearch) { client, _ ->
                if (client.priceCheckerOpen) {
                    client.priceCheckerSearchPendingInput = true
                    client.send(SendEnterName("Search for item:"))
                }
                true
            },
            buttonBinding(-1, 3, "price_checker.mode_value", buttonPriceValue) { client, _ ->
                if (client.priceCheckerOpen) {
                    client.priceCheckerSetMode(0)
                }
                true
            },
            buttonBinding(-1, 4, "price_checker.mode_alch", buttonPriceAlch) { client, _ ->
                if (client.priceCheckerOpen) {
                    client.priceCheckerSetMode(1)
                }
                true
            }
        )
}
