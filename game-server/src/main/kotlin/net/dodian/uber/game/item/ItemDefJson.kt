package net.dodian.uber.game.item

import com.google.gson.annotations.SerializedName

data class ItemDefJson(
    val id: Int,
    val name: String,
    val members: Boolean = false,
    val tradeable: Boolean = false,
    val stackable: Boolean = false,
    val noted: Boolean = false,
    val noteable: Boolean = false,
    val placeholder: Boolean = false,
    @SerializedName("linked_id_item") val linkedIdItem: Int? = null,
    @SerializedName("linked_id_noted") val linkedIdNoted: Int? = null,
    val cost: Int = 0,
    @SerializedName("lowalch") val lowAlch: Int = 0,
    @SerializedName("highalch") val highAlch: Int = 0,
    val weight: Double = 0.0,
    val examine: String? = null,
    val equipment: ItemEquipmentDef? = null,
    val weapon: ItemWeaponDef? = null
)
