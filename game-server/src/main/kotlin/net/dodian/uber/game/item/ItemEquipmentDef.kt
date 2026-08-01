package net.dodian.uber.game.item

import com.google.gson.annotations.SerializedName

data class ItemEquipmentDef(
    @SerializedName("attack_stab") val attackStab: Int = 0,
    @SerializedName("attack_slash") val attackSlash: Int = 0,
    @SerializedName("attack_crush") val attackCrush: Int = 0,
    @SerializedName("attack_magic") val attackMagic: Int = 0,
    @SerializedName("attack_ranged") val attackRanged: Int = 0,

    @SerializedName("defence_stab") val defenceStab: Int = 0,
    @SerializedName("defence_slash") val defenceSlash: Int = 0,
    @SerializedName("defence_crush") val defenceCrush: Int = 0,
    @SerializedName("defence_magic") val defenceMagic: Int = 0,
    @SerializedName("defence_ranged") val defenceRanged: Int = 0,

    @SerializedName("melee_strength") val meleeStrength: Int = 0,
    @SerializedName("ranged_strength") val rangedStrength: Int = 0,
    @SerializedName("magic_damage") val magicDamage: Int = 0,
    val prayer: Int = 0,

    val slot: String = "",
    val requirements: ItemRequirement? = null
) {
    fun toBonusArray(): IntArray = intArrayOf(
        attackStab, attackSlash, attackCrush, attackMagic, attackRanged,
        defenceStab, defenceSlash, defenceCrush, defenceMagic, defenceRanged,
        meleeStrength, rangedStrength, magicDamage, prayer
    )
}
