package net.dodian.game.content.skills.smithing

data class SmeltingRequest(
    val recipe: SmeltingRecipe,
    val amount: Int,
)
