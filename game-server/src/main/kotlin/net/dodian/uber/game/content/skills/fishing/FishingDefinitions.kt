package net.dodian.uber.game.content.skills.fishing

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class FishingSpotDefinition(
    val index: Int,
    val objectId: Int,
    val clickType: Int,
    val fishItemId: Int,
    val animationId: Int,
    val requiredLevel: Int,
    val baseDelayMs: Int,
    val toolItemId: Int,
    val experience: Int,
    val premiumOnly: Boolean = false,
    val featherConsumed: Boolean = false,
)

object FishingDefinitions {
    val fishingSpots: List<FishingSpotDefinition>
        get() = SkillDataRegistry.fishingSpots()

    @JvmStatic
    fun byIndex(index: Int): FishingSpotDefinition? = fishingSpots.getOrNull(index)

    @JvmStatic
    fun findSpot(objectId: Int, clickType: Int): FishingSpotDefinition? =
        fishingSpots.firstOrNull { it.objectId == objectId && it.clickType == clickType }
}
