package net.dodian.uber.game.content.interfaces.ui

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object UiComponents {
    val runOffButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().runOffButtons
    val runOnButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().runOnButtons
    val runToggleButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().runToggleButtons

    val tabInterfaceDefaultButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().tabInterfaceDefaultButtons
    val tabInterfaceEquipmentButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().tabInterfaceEquipmentButtons

    val sidebarHomeButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().sidebarHomeButtons
    val closeInterfaceButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().closeInterfaceButtons
    val questTabToggleButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().questTabToggleButtons
    val logoutButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().logoutButtons
    val morphButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().morphButtons
    val ignoredButtons: IntArray
        get() = InterfaceMappingRegistry.uiData().ignoredButtons
}
