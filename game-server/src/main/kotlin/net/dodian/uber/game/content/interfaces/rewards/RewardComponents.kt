package net.dodian.uber.game.content.interfaces.rewards

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object RewardComponents {
    val skillSelectionButtons: IntArray
        get() = InterfaceMappingRegistry.rewardData().skillSelectionButtons
}
