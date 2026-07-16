package net.dodian.uber.skills.runtime

import net.dodian.uber.skills.api.SkillRecipe

/** Pure recipe planning equivalent to OpenRune's skill-multi availability calculation. */
object SkillRecipePlanner {
    fun maxCraftable(recipe: SkillRecipe, itemAmounts: (Int) -> Int): Int {
        if (recipe.materials.isEmpty()) return 0
        return recipe.materials.minOf { material ->
            (itemAmounts(material.itemId).coerceAtLeast(0) / material.amount).coerceAtLeast(0)
        }
    }

    fun available(recipes: Iterable<SkillRecipe>, itemAmounts: (Int) -> Int): List<Pair<SkillRecipe, Int>> =
        recipes.mapNotNull { recipe -> maxCraftable(recipe, itemAmounts).takeIf { it > 0 }?.let { recipe to it } }
}
