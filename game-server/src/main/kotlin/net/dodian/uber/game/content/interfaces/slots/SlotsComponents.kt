package net.dodian.uber.game.content.interfaces.slots

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object SlotsComponents {
    val spinButtons: IntArray
        get() = InterfaceMappingRegistry.slotsData().spinButtons
}
