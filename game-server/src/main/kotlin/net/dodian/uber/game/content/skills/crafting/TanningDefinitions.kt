package net.dodian.uber.game.content.skills.crafting

import net.dodian.uber.game.content.platform.SkillDataRegistry

object TanningDefinitions {
    val definitions: List<TanningDefinition>
        get() = SkillDataRegistry.craftingTanningDefinitions()

    @JvmStatic
    fun find(hideType: Int): TanningDefinition? = definitions.firstOrNull { it.hideType == hideType }
}
