package net.dodian.uber.game.content.skills.crafting

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class HideDefinition(
    val itemId: Int,
    val experience: Int,
    val glovesId: Int,
    val glovesLevel: Int,
    val chapsId: Int,
    val chapsLevel: Int,
    val bodyId: Int,
    val bodyLevel: Int,
)

data class GemDefinition(
    val uncutId: Int,
    val cutId: Int,
    val requiredLevel: Int,
    val experience: Int,
    val animationId: Int,
)

data class OrbDefinition(
    val orbId: Int,
    val staffId: Int,
    val requiredLevel: Int,
    val experience: Int,
)

object CraftingDefinitions {
    val hideDefinitions: List<HideDefinition>
        get() = SkillDataRegistry.craftingHideDefinitions()

    val gemDefinitions: List<GemDefinition>
        get() = SkillDataRegistry.craftingGemDefinitions()

    val orbDefinitions: List<OrbDefinition>
        get() = SkillDataRegistry.craftingOrbDefinitions()

    @JvmStatic
    fun hideDefinition(index: Int): HideDefinition? = hideDefinitions.getOrNull(index)

    @JvmStatic
    fun findHideDefinition(itemId: Int): HideDefinition? = hideDefinitions.firstOrNull { it.itemId == itemId }

    @JvmStatic
    fun findGemDefinition(uncutId: Int): GemDefinition? = gemDefinitions.firstOrNull { it.uncutId == uncutId }

    @JvmStatic
    fun findOrbDefinition(orbId: Int): OrbDefinition? = orbDefinitions.firstOrNull { it.orbId == orbId }
}
