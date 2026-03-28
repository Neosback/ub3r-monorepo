package net.dodian.uber.game.content.skills.runecrafting

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class RunecraftingAltarDefinition(
    val objectId: Int,
    val request: RunecraftingRequest,
)

object RunecraftingDefinitions {
    val RUNE_ESSENCE_ID: Int
        get() = SkillDataRegistry.runecraftingRuneEssenceId()

    private val altarDefinitions: List<RunecraftingAltarDefinition>
        get() = SkillDataRegistry.runecraftingAltars()

    @JvmField
    val altarObjectIds: IntArray = altarDefinitions.map { it.objectId }.toIntArray()

    @JvmStatic
    fun byObjectId(objectId: Int): RunecraftingAltarDefinition? = altarDefinitions.firstOrNull { it.objectId == objectId }
}
