package net.dodian.uber.game.content.objects.banking

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object BankingObjectComponents {
    val boothObjects: IntArray
        get() = InterfaceMappingRegistry.bankingObjectsData().boothObjects
    val chestObjects: IntArray
        get() = InterfaceMappingRegistry.bankingObjectsData().chestObjects
}
