package net.dodian.game.content.interfaces.partyroom

import net.dodian.game.content.events.partyroom.Balloons
import net.dodian.game.systems.ui.buttons.InterfaceButtonContent
import net.dodian.game.systems.ui.buttons.buttonBinding

object PartyRoomInterfaceButtons : InterfaceButtonContent {
    override val bindings =
        listOf(
            buttonBinding(
                interfaceId = -1,
                componentId = 0,
                componentKey = "partyroom.deposit.accept",
                rawButtonIds = PartyRoomComponents.depositAcceptButtons,
            ) { client, _ ->
                Balloons.acceptItems(client)
                true
            }
        )
}
