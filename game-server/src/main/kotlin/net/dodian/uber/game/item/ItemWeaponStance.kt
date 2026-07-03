package net.dodian.uber.game.item

import com.google.gson.annotations.SerializedName

data class ItemWeaponStance(
    @SerializedName("combat_style") val combatStyle: String,
    @SerializedName("attack_type") val attackType: String?,
    @SerializedName("attack_style") val attackStyle: String?,
    val experience: String,
    val boosts: String?
)
