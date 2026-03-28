package net.dodian.uber.game.content.skills.smithing

import net.dodian.uber.game.content.platform.SkillDataRegistry

object SmithingDefinitions {
    private val smithingData
        get() = SkillDataRegistry.smithingData()

    private val smithingPageSlots: Map<Int, IntArray>
        get() = smithingData.smithingPageSlots.associate { it.frameId to it.indices.toIntArray() }

    val smeltingRecipes: List<SmeltingRecipe>
        get() = smithingData.smeltingRecipes

    val smeltingButtonMappings: List<FurnaceButtonMapping>
        get() = smithingData.smeltingButtonMappings

    val smithingTiers: List<SmithingTier>
        get() = smithingData.smithingTiers

    @JvmStatic
    fun findSmeltingRecipe(barId: Int): SmeltingRecipe? = smeltingRecipes.firstOrNull { it.barId == barId }

    @JvmStatic
    fun findSmeltingRecipeByOre(itemId: Int): SmeltingRecipe? =
        smeltingRecipes.firstOrNull { recipe -> recipe.oreRequirements.any { it.itemId == itemId } }

    @JvmStatic
    fun findSmithingTierByBar(barId: Int): SmithingTier? = smithingTiers.firstOrNull { it.barId == barId }

    @JvmStatic
    fun findSmithingTierByTypeId(typeId: Int): SmithingTier? = smithingTiers.firstOrNull { it.typeId == typeId }

    @JvmStatic
    fun findTierForProduct(itemId: Int): SmithingTier? = smithingTiers.firstOrNull { tier -> tier.products.any { it.itemId == itemId } }

    @JvmStatic
    fun displayItemsForFrame(tier: SmithingTier, frameId: Int): List<SmithingDisplayItem> {
        val indices = smithingPageSlots[frameId] ?: return emptyList()
        return indices.map { index ->
            val product = tier.products.getOrNull(index)
            if (product == null) {
                SmithingDisplayItem(-1, 0)
            } else {
                SmithingDisplayItem(product.itemId, product.outputAmount)
            }
        }
    }

    @JvmStatic
    fun frameIds(): IntArray = smithingData.smeltFrameIds.toIntArray()

    @JvmStatic
    fun isSmeltingInterfaceButton(buttonId: Int): Boolean =
        smeltingButtonMappings.any { it.buttonId == buttonId } || smithingData.smeltFrameIds.contains(buttonId)
}
