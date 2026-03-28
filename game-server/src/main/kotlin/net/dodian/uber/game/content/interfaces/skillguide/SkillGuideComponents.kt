package net.dodian.uber.game.content.interfaces.skillguide

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object SkillGuideComponents {
    const val SKILL_GUIDE_INTERFACE_ID = 8717
    const val INTERFACE_ID = SKILL_GUIDE_INTERFACE_ID

    data class SkillGuideButtonGroup(
        val skillId: Int,
        val componentId: Int,
        val componentKey: String,
        val rawButtonIds: IntArray,
    )

    data class SubTabDefinition(
        val componentId: Int,
        val componentKey: String,
        val rawButtonIds: IntArray,
        val targetTab: Int,
    )

    private val loadedData by lazy { InterfaceMappingRegistry.skillGuideData() }

    val skillButtons: List<SkillGuideButtonGroup>
        get() = loadedData.skillButtons

    val subTabs: List<SubTabDefinition>
        get() = loadedData.subTabs
}
