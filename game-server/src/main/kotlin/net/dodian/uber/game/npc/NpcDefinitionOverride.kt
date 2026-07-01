package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition

data class NpcCacheOverride(
    val id: Int,
    val name: String? = null,
    val examine: String? = null,
    val size: Int? = null,
    val combatLevel: Int? = null,
    val standingAnimation: Int? = null,
    val walkingAnimation: Int? = null,
    val halfTurnAnimation: Int? = null,
    val clockwiseTurnAnimation: Int? = null,
    val anticlockwiseTurnAnimation: Int? = null,
) {
    fun applyTo(definition: CacheNpcDefinition) {
        name?.let { definition.name = it }
        examine?.let { definition.examine = it }
        size?.let { definition.size = it }
        combatLevel?.let { definition.combatLevel = it }
        standingAnimation?.let { definition.standingAnimation = it }
        walkingAnimation?.let { definition.walkingAnimation = it }
        halfTurnAnimation?.let { definition.halfTurnAnimation = it }
        clockwiseTurnAnimation?.let { definition.clockwiseTurnAnimation = it }
        anticlockwiseTurnAnimation?.let { definition.anticlockwiseTurnAnimation = it }
    }
}

data class NpcRuntimeDefinition(
    val id: Int,
    val attackAnimation: Int? = null,
    val deathAnimation: Int? = null,
    val respawnTicks: Int? = null,
    val attack: Int? = null,
    val strength: Int? = null,
    val defence: Int? = null,
    val hitpoints: Int? = null,
    val ranged: Int? = null,
    val magic: Int? = null,
)

class NpcCacheOverrideBuilder internal constructor(private val id: Int) {
    var name: String? = null
    var examine: String? = null
    var size: Int? = null
    var combatLevel: Int? = null
    var standingAnimation: Int? = null
    var walkingAnimation: Int? = null
    var halfTurnAnimation: Int? = null
    var clockwiseTurnAnimation: Int? = null
    var anticlockwiseTurnAnimation: Int? = null

    internal fun build(): NpcCacheOverride =
        NpcCacheOverride(
            id = id,
            name = cleanText(name),
            examine = cleanText(examine),
            size = positive(size),
            combatLevel = nonNegative(combatLevel),
            standingAnimation = positive(standingAnimation),
            walkingAnimation = positive(walkingAnimation),
            halfTurnAnimation = positive(halfTurnAnimation),
            clockwiseTurnAnimation = positive(clockwiseTurnAnimation),
            anticlockwiseTurnAnimation = positive(anticlockwiseTurnAnimation),
        )
}

/**
 * Server-owned values that are currently consumed by NpcData or per-spawn NPC state.
 * Keep future combat fields in the import audit until combat logic reads them.
 */
class NpcRuntimeDefinitionBuilder internal constructor(private val id: Int) {
    var attackAnimation: Int? = null
    var deathAnimation: Int? = null
    var respawnTicks: Int? = null
    var attack: Int? = null
    var strength: Int? = null
    var defence: Int? = null
    var hitpoints: Int? = null
    var ranged: Int? = null
    var magic: Int? = null

    internal fun build(): NpcRuntimeDefinition =
        NpcRuntimeDefinition(
            id = id,
            attackAnimation = positive(attackAnimation),
            deathAnimation = positive(deathAnimation),
            respawnTicks = positive(respawnTicks),
            attack = positive(attack),
            strength = positive(strength),
            defence = positive(defence),
            hitpoints = positive(hitpoints),
            ranged = positive(ranged),
            magic = positive(magic),
        )
}

private fun cleanText(value: String?): String? =
    value
        ?.replace('_', ' ')
        ?.trim()
        ?.takeIf { it.isNotEmpty() && !it.equals("no name", true) && !it.equals("null", true) }

private fun positive(value: Int?): Int? = value?.takeIf { it > 0 }
private fun nonNegative(value: Int?): Int? = value?.takeIf { it >= 0 }
