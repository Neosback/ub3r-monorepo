package net.dodian.uber.game.content.interfaces.bank

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object BankComponents {
    const val INTERFACE_ID = 5292

    val depositInventoryButtons: IntArray
        get() = InterfaceMappingRegistry.bankData().depositInventoryButtons
    val depositWornItemsButtons: IntArray
        get() = InterfaceMappingRegistry.bankData().depositWornItemsButtons
    val withdrawAsNoteButtons: IntArray
        get() = InterfaceMappingRegistry.bankData().withdrawAsNoteButtons
    val withdrawAsItemButtons: IntArray
        get() = InterfaceMappingRegistry.bankData().withdrawAsItemButtons
    val searchButtons: IntArray
        get() = InterfaceMappingRegistry.bankData().searchButtons

    val tabButtons: List<Int>
        get() = InterfaceMappingRegistry.bankData().tabButtons.toList()
}
