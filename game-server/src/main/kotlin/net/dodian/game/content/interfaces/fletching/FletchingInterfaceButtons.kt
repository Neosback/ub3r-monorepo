package net.dodian.game.content.interfaces.fletching

import net.dodian.game.content.skills.fletching.FletchingPlugin
import net.dodian.game.systems.ui.buttons.InterfaceButtonContent
import net.dodian.game.systems.ui.buttons.buttonBinding

object FletchingInterfaceButtons : InterfaceButtonContent {
    override val bindings =
        listOf(
            buttonBinding(-1, 0, "fletching.bows.longbow", FletchingInterfaceComponents.longbowButtons) { client, request ->
                val amount = FletchingInterfaceComponents.amountByButton[request.rawButtonId] ?: return@buttonBinding false
                FletchingPlugin.start(client, true, amount)
                true
            },
            buttonBinding(-1, 1, "fletching.bows.shortbow", FletchingInterfaceComponents.shortbowButtons) { client, request ->
                val amount = FletchingInterfaceComponents.amountByButton[request.rawButtonId] ?: return@buttonBinding false
                FletchingPlugin.start(client, false, amount)
                true
            },
        )
}
