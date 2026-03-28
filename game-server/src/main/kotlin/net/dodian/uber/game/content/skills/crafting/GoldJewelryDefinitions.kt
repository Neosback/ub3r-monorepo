package net.dodian.uber.game.content.skills.crafting

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class GoldJewelryProduct(
    val index: Int,
    val slot: Int,
    val productId: Int,
    val requiredLevel: Int,
    val experience: Int,
)

object GoldJewelryDefinitions {
    private val data
        get() = SkillDataRegistry.craftingGoldJewelryDefinition()

    val blanks: Array<IntArray>
        get() = data.groups.map { it.blankItemIds.toIntArray() }.toTypedArray()

    val interfaceSlots: IntArray
        get() = data.groups.map { it.interfaceSlot }.toIntArray()

    val requiredGemItems: IntArray
        get() = data.requiredGemItems.toIntArray()

    val blackFrames: IntArray
        get() = data.groups.map { it.blackFrameId }.toIntArray()

    val frameSizes: IntArray
        get() = data.groups.map { it.frameSize }.toIntArray()

    val requiredMoulds: IntArray
        get() = data.groups.map { it.requiredMouldId }.toIntArray()

    val strungAmulets: IntArray
        get() = data.strungAmulets.toIntArray()

    val jewelryByGroup: Array<IntArray>
        get() = data.groups.map { it.jewelryItemIds.toIntArray() }.toTypedArray()

    val levelsByGroup: Array<IntArray>
        get() = data.groups.map { it.requiredLevels.toIntArray() }.toTypedArray()

    val experienceByGroup: Array<IntArray>
        get() = data.groups.map { it.experienceByTen.toIntArray() }.toTypedArray()

    private val products: List<GoldJewelryProduct> =
        buildList {
            levelsByGroup.forEachIndexed { index, levels ->
                levels.forEachIndexed { slot, level ->
                    add(
                        GoldJewelryProduct(
                            index = index,
                            slot = slot,
                            productId = jewelryByGroup[index][slot],
                            requiredLevel = level,
                            experience = experienceByGroup[index][slot] * 10,
                        ),
                    )
                }
            }
        }

    @JvmStatic
    fun product(index: Int, slot: Int): GoldJewelryProduct? = products.firstOrNull { it.index == index && it.slot == slot }

    @JvmStatic
    fun findProductByAmulet(amuletId: Int): GoldJewelryProduct? =
        products.firstOrNull { it.index == 2 && it.productId == amuletId }
}
