package net.dodian.uber.game.content.skills.herblore

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class HerbDefinition(
    val grimyId: Int,
    val cleanId: Int,
    val unfinishedPotionId: Int,
    val requiredLevel: Int,
    val cleaningExperience: Int,
    val premiumOnly: Boolean,
)

data class PotionRecipeDefinition(
    val unfinishedPotionId: Int,
    val secondaryId: Int,
    val finishedPotionId: Int,
    val requiredLevel: Int,
    val experience: Int,
    val premiumOnly: Boolean,
)

data class PotionDoseDefinition(
    val oneDoseId: Int,
    val twoDoseId: Int,
    val threeDoseId: Int,
    val fourDoseId: Int,
)

object HerbloreDefinitions {
    const val BATCH_INTERFACE_ID: Int = 4753
    const val VIAL_OF_WATER_ID: Int = 228
    const val EMPTY_VIAL_ID: Int = 229
    const val UNFINISHED_POTION_VIAL_ID: Int = 227
    const val GRIND_COST_PER_HERB: Int = 200
    const val UNFINISHED_POTION_COST: Int = 1_000

    val herbDefinitions: List<HerbDefinition>
        get() = SkillDataRegistry.herbloreHerbDefinitions()

    val potionRecipes: List<PotionRecipeDefinition>
        get() = SkillDataRegistry.herblorePotionRecipes()

    val potionDoseDefinitions: List<PotionDoseDefinition>
        get() = SkillDataRegistry.herblorePotionDoseDefinitions()

    @JvmStatic
    fun findHerbDefinitionByGrimy(grimyId: Int): HerbDefinition? = herbDefinitions.firstOrNull { it.grimyId == grimyId }

    @JvmStatic
    fun findHerbDefinitionByClean(cleanId: Int): HerbDefinition? = herbDefinitions.firstOrNull { it.cleanId == cleanId }

    @JvmStatic
    fun findPotionRecipe(unfinishedPotionId: Int, secondaryId: Int): PotionRecipeDefinition? =
        potionRecipes.firstOrNull { it.unfinishedPotionId == unfinishedPotionId && it.secondaryId == secondaryId }

    @JvmStatic
    fun findPotionDoseByAny(itemId: Int): PotionDoseDefinition? =
        potionDoseDefinitions.firstOrNull {
            it.oneDoseId == itemId || it.twoDoseId == itemId || it.threeDoseId == itemId || it.fourDoseId == itemId
        }
}
