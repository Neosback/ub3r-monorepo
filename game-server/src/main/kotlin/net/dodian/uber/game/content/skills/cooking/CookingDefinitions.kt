package net.dodian.uber.game.content.skills.cooking

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class CookingDefinition(
    val rawItemId: Int,
    val cookedItemId: Int,
    val burntItemId: Int,
    val experience: Int,
    val requiredLevel: Int,
    val burnRollBase: Int,
)

object CookingDefinitions {
    val recipes: List<CookingDefinition>
        get() = SkillDataRegistry.cookingRecipes()

    @JvmStatic
    fun findRecipe(rawItemId: Int): CookingDefinition? = recipes.firstOrNull { it.rawItemId == rawItemId }

    @JvmStatic
    fun recipeByIndex(index: Int): CookingDefinition? = recipes.getOrNull(index)
}
