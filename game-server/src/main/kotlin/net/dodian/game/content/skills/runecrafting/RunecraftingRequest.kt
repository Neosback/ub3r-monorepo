package net.dodian.game.content.skills.runecrafting

data class RunecraftingRequest(
    val runeId: Int,
    val requiredLevel: Int,
    val experiencePerEssence: Int,
)
