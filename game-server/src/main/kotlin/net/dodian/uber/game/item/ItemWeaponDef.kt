package net.dodian.uber.game.item

import com.google.gson.annotations.SerializedName

data class ItemWeaponDef(
    @SerializedName("attack_speed") val attackSpeed: Int = 4,
    @SerializedName("weapon_type") val weaponType: String = "",
    val stances: Array<ItemWeaponStance> = emptyArray()
)
