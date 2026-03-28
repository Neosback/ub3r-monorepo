package net.dodian.uber.game.content.skills.thieving

enum class ThievingType {
    PICKPOCKETING,
    STALL_THIEVING,
    OTHER,
}

data class ThievingReward(
    val itemId: Int,
    val minAmount: Int,
    val maxAmount: Int,
    val chance: Int,
) {
    fun amount(): Int = minAmount + (Math.random() * (maxAmount - minAmount + 1)).toInt()
}

data class ThievingDefinition(
    val name: String,
    val entityId: Int,
    val requiredLevel: Int,
    val receivedExperience: Int,
    val rewards: List<ThievingReward> = emptyList(),
    val respawnTime: Int,
    val type: ThievingType,
)
