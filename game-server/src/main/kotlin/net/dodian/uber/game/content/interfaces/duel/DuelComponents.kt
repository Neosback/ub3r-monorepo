package net.dodian.uber.game.content.interfaces.duel

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object DuelComponents {
    val offerRuleButtons: IntArray
        get() = InterfaceMappingRegistry.duelData().offerRuleButtons
    val offerRuleIndexByButton: Map<Int, Int>
        get() = InterfaceMappingRegistry.duelData().offerRuleIndices.associate { it.buttonId to it.ruleIndex }

    val bodyRuleButtons: IntArray
        get() = InterfaceMappingRegistry.duelData().bodyRuleButtons

    const val CONFIRM_STAGE_TWO_BUTTON = 6520
    const val CONFIRM_STAGE_ONE_BUTTON = 6674
}
