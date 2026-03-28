package net.dodian.uber.game.content.skills.fletching

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class FletchingLogDefinition(
    val logItemId: Int,
    val unstrungShortbowId: Int,
    val unstrungLongbowId: Int,
    val shortbowId: Int,
    val longbowId: Int,
    val shortLevelRequired: Int,
    val longLevelRequired: Int,
    val shortExperience: Int,
    val longExperience: Int,
    val shortStringAnimationId: Int,
    val longStringAnimationId: Int,
)

data class ArrowRecipe(
    val headId: Int,
    val arrowId: Int,
    val requiredLevel: Int,
    val experience: Int,
)

data class DartRecipe(
    val tipId: Int,
    val dartId: Int,
    val requiredLevel: Int,
    val experience: Int,
)

object FletchingDefinitions {
    val bowLogs: List<FletchingLogDefinition>
        get() = SkillDataRegistry.fletchingBowLogs()

    val arrowRecipes: List<ArrowRecipe>
        get() = SkillDataRegistry.fletchingArrowRecipes()

    val dartRecipes: List<DartRecipe>
        get() = SkillDataRegistry.fletchingDartRecipes()

    private val bowWeaponIds: Set<Int> =
        buildSet {
            bowLogs.forEach { bow ->
                add(bow.shortbowId)
                add(bow.longbowId)
            }
            addAll(SkillDataRegistry.fletchingExtraBowWeaponIds())
            addAll(12765..12768)
        }

    @JvmStatic
    fun bowLog(index: Int): FletchingLogDefinition? = bowLogs.getOrNull(index)

    @JvmStatic
    fun findBowLogByLog(logItemId: Int): FletchingLogDefinition? = bowLogs.firstOrNull { it.logItemId == logItemId }

    @JvmStatic
    fun findArrowRecipeByHead(headId: Int): ArrowRecipe? = arrowRecipes.firstOrNull { it.headId == headId }

    @JvmStatic
    fun findDartRecipeByTip(tipId: Int): DartRecipe? = dartRecipes.firstOrNull { it.tipId == tipId }

    @JvmStatic
    fun isBowWeapon(itemId: Int): Boolean = bowWeaponIds.contains(itemId)
}
