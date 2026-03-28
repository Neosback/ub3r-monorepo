package net.dodian.uber.game.content.platform

import net.dodian.uber.game.content.skills.cooking.CookingDefinition
import net.dodian.uber.game.content.skills.fishing.FishingSpotDefinition
import net.dodian.uber.game.content.skills.fletching.ArrowRecipe
import net.dodian.uber.game.content.skills.fletching.DartRecipe
import net.dodian.uber.game.content.skills.fletching.FletchingLogDefinition

private data class CookingDataFile(
    val recipes: List<CookingDefinition> = emptyList(),
)

private data class FishingDataFile(
    val fishingSpots: List<FishingSpotDefinition> = emptyList(),
)

private data class FletchingDataFile(
    val bowLogs: List<FletchingLogDefinition> = emptyList(),
    val arrowRecipes: List<ArrowRecipe> = emptyList(),
    val dartRecipes: List<DartRecipe> = emptyList(),
    val extraBowWeaponIds: List<Int> = emptyList(),
)

object SkillDataRegistry {
    @Volatile
    private var cookingOverride: CookingDataFile? = null

    @Volatile
    private var fishingOverride: FishingDataFile? = null

    @Volatile
    private var fletchingOverride: FletchingDataFile? = null

    @JvmStatic
    fun cookingRecipes(fallback: List<CookingDefinition>): List<CookingDefinition> {
        val cached = cookingOverride ?: ContentDataLoader.loadOptional<CookingDataFile>("content/skills/cooking.toml").also {
            cookingOverride = it
        }
        return if (cached?.recipes.isNullOrEmpty()) fallback else cached!!.recipes
    }

    @JvmStatic
    fun fishingSpots(fallback: List<FishingSpotDefinition>): List<FishingSpotDefinition> {
        val cached = fishingOverride ?: ContentDataLoader.loadOptional<FishingDataFile>("content/skills/fishing.toml").also {
            fishingOverride = it
        }
        return if (cached?.fishingSpots.isNullOrEmpty()) fallback else cached!!.fishingSpots
    }

    @JvmStatic
    fun fletchingBowLogs(fallback: List<FletchingLogDefinition>): List<FletchingLogDefinition> {
        val cached = fletchingOverride ?: ContentDataLoader.loadOptional<FletchingDataFile>("content/skills/fletching.toml").also {
            fletchingOverride = it
        }
        return if (cached?.bowLogs.isNullOrEmpty()) fallback else cached!!.bowLogs
    }

    @JvmStatic
    fun fletchingArrowRecipes(fallback: List<ArrowRecipe>): List<ArrowRecipe> {
        val cached = fletchingOverride ?: ContentDataLoader.loadOptional<FletchingDataFile>("content/skills/fletching.toml").also {
            fletchingOverride = it
        }
        return if (cached?.arrowRecipes.isNullOrEmpty()) fallback else cached!!.arrowRecipes
    }

    @JvmStatic
    fun fletchingDartRecipes(fallback: List<DartRecipe>): List<DartRecipe> {
        val cached = fletchingOverride ?: ContentDataLoader.loadOptional<FletchingDataFile>("content/skills/fletching.toml").also {
            fletchingOverride = it
        }
        return if (cached?.dartRecipes.isNullOrEmpty()) fallback else cached!!.dartRecipes
    }

    @JvmStatic
    fun fletchingExtraBowWeaponIds(fallback: Set<Int>): Set<Int> {
        val cached = fletchingOverride ?: ContentDataLoader.loadOptional<FletchingDataFile>("content/skills/fletching.toml").also {
            fletchingOverride = it
        }
        return if (cached?.extraBowWeaponIds.isNullOrEmpty()) fallback else cached!!.extraBowWeaponIds.toSet()
    }
}
