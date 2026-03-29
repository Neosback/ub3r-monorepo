package net.dodian.game.content.interfaces.slots

import net.dodian.game.Server
import net.dodian.game.systems.ui.buttons.InterfaceButtonContent
import net.dodian.game.systems.ui.buttons.buttonBinding

object SlotsInterfaceButtons : InterfaceButtonContent {
    override val bindings =
        listOf(
            buttonBinding(-1, 0, "slots.spin", SlotsComponents.spinButtons) { client, _ ->
                Server.slots.playSlots(client, -1)
                true
            }
        )
}
