package net.dodian.uber.game.content.skills.thieving.plunder

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class PyramidPlunderPlayerState(
    var ticksRemaining: Int = -1,
    var roomNumber: Int = 0,
    var looting: Boolean = false,
    val obstacles: IntArray = SkillDataRegistry.thievingPlunderData().obstacles.toIntArray(),
) {
    val urnConfig: IntArray = SkillDataRegistry.thievingPlunderData().urnConfig.toIntArray()
    val tombConfig: IntArray = SkillDataRegistry.thievingPlunderData().tombConfig.toIntArray()
}
