package net.dodian.uber.game.ui

import net.dodian.uber.game.activity.partyroom.PartyRoomBalloons
import net.dodian.uber.game.ui.buttons.InterfaceButtonContent
import net.dodian.uber.game.ui.buttons.buttonBinding

object PartyRoomInterface : InterfaceButtonContent {
    private const val INTERFACE_ID = 2156
    private const val ACCEPT_COMPONENT_ID = 2246
    private val depositAcceptButtons = intArrayOf(ACCEPT_COMPONENT_ID)

    override val bindings =
        listOf(
            buttonBinding(
                interfaceId = INTERFACE_ID,
                componentId = ACCEPT_COMPONENT_ID,
                componentKey = "partyroom.deposit.accept",
                rawButtonIds = depositAcceptButtons,
                requiredInterfaceId = INTERFACE_ID,
            ) { client, _ ->
                PartyRoomBalloons.acceptOfferedPartyItems(client)
                true
            },
        )
}
