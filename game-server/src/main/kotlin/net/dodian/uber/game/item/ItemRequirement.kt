package net.dodian.uber.game.item

data class ItemRequirement(
    val attack: Int = 0,
    val defence: Int = 0,
    val strength: Int = 0,
    val hitpoints: Int = 0,
    val ranged: Int = 0,
    val prayer: Int = 0,
    val magic: Int = 0,
    val cooking: Int = 0,
    val woodcutting: Int = 0,
    val fletching: Int = 0,
    val fishing: Int = 0,
    val firemaking: Int = 0,
    val crafting: Int = 0,
    val smithing: Int = 0,
    val mining: Int = 0,
    val herblore: Int = 0,
    val agility: Int = 0,
    val thieving: Int = 0,
    val slayer: Int = 0,
    val farming: Int = 0,
    val runecrafting: Int = 0,
    val hunter: Int = 0
)

fun ItemRequirement?.toRequiredArray(): IntArray =
    if (this == null) IntArray(23)
    else intArrayOf(attack, defence, strength, hitpoints, ranged, prayer, magic,
        cooking, woodcutting, fletching, fishing, firemaking, crafting, smithing,
        mining, herblore, agility, thieving, slayer, farming, runecrafting, hunter)
