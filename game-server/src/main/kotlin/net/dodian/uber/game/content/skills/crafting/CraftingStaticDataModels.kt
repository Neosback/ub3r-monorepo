package net.dodian.uber.game.content.skills.crafting

data class StandardLeatherCraftDefinition(
    val productId: Int,
    val requiredLevel: Int,
    val experience: Int,
)

data class TanningDefinition(
    val hideType: Int,
    val hideId: Int,
    val leatherId: Int,
    val coinCost: Int,
    val displayName: String,
)

data class GoldJewelryGroupData(
    val groupId: Int,
    val interfaceSlot: Int,
    val requiredMouldId: Int,
    val frameSize: Int,
    val blackFrameId: Int,
    val blankItemIds: List<Int> = emptyList(),
    val jewelryItemIds: List<Int> = emptyList(),
    val requiredLevels: List<Int> = emptyList(),
    val experienceByTen: List<Int> = emptyList(),
)

data class GoldJewelryDefinitionData(
    val requiredGemItems: List<Int> = emptyList(),
    val strungAmulets: List<Int> = emptyList(),
    val groups: List<GoldJewelryGroupData> = emptyList(),
)
