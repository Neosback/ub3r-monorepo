package net.dodian.uber.game.content.interfaces.partyroom

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object PartyRoomComponents {
    val depositAcceptButtons: IntArray
        get() = InterfaceMappingRegistry.partyRoomData().depositAcceptButtons
}
