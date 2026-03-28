package net.dodian.uber.game.content.objects.events

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object PartyRoomObjectComponents {
    val balloonObjects: IntArray
        get() = InterfaceMappingRegistry.partyRoomObjectsData().balloonObjects
    val DEPOSIT_CHEST: Int
        get() = InterfaceMappingRegistry.partyRoomObjectsData().depositChest
    val FORCE_TRIGGER: Int
        get() = InterfaceMappingRegistry.partyRoomObjectsData().forceTrigger
    val allObjects: IntArray
        get() = balloonObjects + intArrayOf(DEPOSIT_CHEST, FORCE_TRIGGER)
}
