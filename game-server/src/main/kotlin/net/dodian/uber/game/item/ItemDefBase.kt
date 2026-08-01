package net.dodian.uber.game.item

import com.google.gson.annotations.SerializedName

data class ItemDefBase(
    val id: Int,
    val name: String,
    @SerializedName("stackable") val stackable: Boolean = false,
    @SerializedName("tradeable") val tradeable: Boolean = true,
    @SerializedName("two-handed") val twoHanded: Boolean = false,
    @SerializedName("noted-id") val notedId: Int? = null,
    @SerializedName("unnoted-id") val unnotedId: Int? = null,
    @SerializedName("base-value") val baseValue: Int = 0,
    @SerializedName("street-value") val streetValue: Int = 0,
    @SerializedName("low-alch") val lowAlch: Int = 0,
    @SerializedName("high-alch") val highAlch: Int = 0,
    val weight: Double = 0.0,
    @SerializedName("destroy-message") val destroyMessage: String? = null,
    @SerializedName("destroyable") val destroyable: Boolean = false,
    @SerializedName("stand-animation") val standAnimation: Int? = null,
    @SerializedName("walk-animation") val walkAnimation: Int? = null,
    @SerializedName("run-animation") val runAnimation: Int? = null,
    @SerializedName("block-animation") val blockAnimation: Int? = null,
    @SerializedName("attack-animations") val attackAnimations: Map<String, Int>? = null,
    @SerializedName("equipment-slot") val equipmentSlot: String? = null,
    @SerializedName("weapon-interface") val weaponInterface: String? = null,
    @SerializedName("req-attack") val reqAttack: Int? = null,
    @SerializedName("req-defence") val reqDefence: Int? = null,
    @SerializedName("req-strength") val reqStrength: Int? = null,
    @SerializedName("req-hitpoints") val reqHitpoints: Int? = null,
    @SerializedName("req-ranged") val reqRanged: Int? = null,
    @SerializedName("req-prayer") val reqPrayer: Int? = null,
    @SerializedName("req-magic") val reqMagic: Int? = null,
    @SerializedName("req-cooking") val reqCooking: Int? = null,
    @SerializedName("req-woodcutting") val reqWoodcutting: Int? = null,
    @SerializedName("req-fletching") val reqFletching: Int? = null,
    @SerializedName("req-fishing") val reqFishing: Int? = null,
    @SerializedName("req-firemaking") val reqFiremaking: Int? = null,
    @SerializedName("req-crafting") val reqCrafting: Int? = null,
    @SerializedName("req-smithing") val reqSmithing: Int? = null,
    @SerializedName("req-mining") val reqMining: Int? = null,
    @SerializedName("req-herblore") val reqHerblore: Int? = null,
    @SerializedName("req-agility") val reqAgility: Int? = null,
    @SerializedName("req-thieving") val reqThieving: Int? = null,
    @SerializedName("req-slayer") val reqSlayer: Int? = null,
    @SerializedName("req-farming") val reqFarming: Int? = null,
    @SerializedName("req-runecrafting") val reqRunecrafting: Int? = null,
    @SerializedName("req-construction") val reqConstruction: Int? = null,
    @SerializedName("req-hunter") val reqHunter: Int? = null,
    @SerializedName("attack-stab") val attackStab: Int? = null,
    @SerializedName("attack-slash") val attackSlash: Int? = null,
    @SerializedName("attack-crush") val attackCrush: Int? = null,
    @SerializedName("attack-magic") val attackMagic: Int? = null,
    @SerializedName("attack-ranged") val attackRanged: Int? = null,
    @SerializedName("defence-stab") val defenceStab: Int? = null,
    @SerializedName("defence-slash") val defenceSlash: Int? = null,
    @SerializedName("defence-crush") val defenceCrush: Int? = null,
    @SerializedName("defence-magic") val defenceMagic: Int? = null,
    @SerializedName("defence-ranged") val defenceRanged: Int? = null,
    @SerializedName("bonus-strength") val bonusStrength: Int? = null,
    @SerializedName("ranged-strength") val rangedStrength: Int? = null,
    @SerializedName("magic-damage") val magicDamage: Int? = null,
    @SerializedName("bonus-prayer") val bonusPrayer: Int? = null,
    @SerializedName("ranged-type") val rangedType: String? = null,
    val allowed: List<String>? = null
) {
    fun toRequirements(): ItemRequirement =
        ItemRequirement(
            attack = reqAttack ?: 0, defence = reqDefence ?: 0,
            strength = reqStrength ?: 0, hitpoints = reqHitpoints ?: 0,
            ranged = reqRanged ?: 0, prayer = reqPrayer ?: 0,
            magic = reqMagic ?: 0, cooking = reqCooking ?: 0,
            woodcutting = reqWoodcutting ?: 0, fletching = reqFletching ?: 0,
            fishing = reqFishing ?: 0, firemaking = reqFiremaking ?: 0,
            crafting = reqCrafting ?: 0, smithing = reqSmithing ?: 0,
            mining = reqMining ?: 0, herblore = reqHerblore ?: 0,
            agility = reqAgility ?: 0, thieving = reqThieving ?: 0,
            slayer = reqSlayer ?: 0, farming = reqFarming ?: 0,
            runecrafting = reqRunecrafting ?: 0, hunter = reqHunter ?: 0
        )

    fun toBonusArray(): IntArray = intArrayOf(
        attackStab ?: 0, attackSlash ?: 0, attackCrush ?: 0, attackMagic ?: 0, attackRanged ?: 0,
        defenceStab ?: 0, defenceSlash ?: 0, defenceCrush ?: 0, defenceMagic ?: 0, defenceRanged ?: 0,
        bonusStrength ?: 0, rangedStrength ?: 0, magicDamage ?: 0, bonusPrayer ?: 0
    )

    fun hasAnyBonus(): Boolean =
        attackStab != null || attackSlash != null || attackCrush != null ||
        attackMagic != null || attackRanged != null || defenceStab != null ||
        defenceSlash != null || defenceCrush != null || defenceMagic != null ||
        defenceRanged != null || bonusStrength != null || rangedStrength != null ||
        magicDamage != null || bonusPrayer != null
}
