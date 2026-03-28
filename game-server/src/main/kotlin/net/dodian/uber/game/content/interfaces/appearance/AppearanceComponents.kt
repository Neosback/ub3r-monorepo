package net.dodian.uber.game.content.interfaces.appearance

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object AppearanceComponents {
    val confirmButtons: IntArray
        get() = InterfaceMappingRegistry.appearanceData().confirmButtons
}
