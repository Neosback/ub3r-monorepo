package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.player.Client

const val NPC_UNSET = -1
const val NORTH = 0
const val NORTH_EAST = 1
const val EAST = 2
const val SOUTH_EAST = 3
const val SOUTH = 4
const val SOUTH_WEST = 5
const val WEST = 6
const val NORTH_WEST = 7

@Suppress("PropertyName")
val north: Int = NORTH
@Suppress("PropertyName")
val northEast: Int = NORTH_EAST
@Suppress("PropertyName")
val east: Int = EAST
@Suppress("PropertyName")
val southEast: Int = SOUTH_EAST
@Suppress("PropertyName")
val south: Int = SOUTH
@Suppress("PropertyName")
val southWest: Int = SOUTH_WEST
@Suppress("PropertyName")
val west: Int = WEST
@Suppress("PropertyName")
val northWest: Int = NORTH_WEST

@JvmInline
value class NpcProfile(val key: String)

fun profile(key: String): NpcProfile = NpcProfile(key.trim().lowercase())

data class NpcSpawnDef(
    val npcId: Int,
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val face: Int = NORTH,
    val rx: Int = 0,
    val ry: Int = 0,
    val rx2: Int = 0,
    val ry2: Int = 0,
    val live: Boolean = true,
    val walkRadius: Int = 0,
    val attackRange: Int = 6,
    val alwaysActive: Boolean = false,
    val condition: (Client) -> Boolean = { true },
    val respawnTicks: Int = NPC_UNSET,
    val attack: Int = NPC_UNSET,
    val defence: Int = NPC_UNSET,
    val strength: Int = NPC_UNSET,
    val hitpoints: Int = NPC_UNSET,
    val ranged: Int = NPC_UNSET,
    val magic: Int = NPC_UNSET,
    val attackAnimation: Int = NPC_UNSET,
    val deathAnimation: Int = NPC_UNSET,
    val profile: String? = null,
)

class NpcSpawnOverrideBuilder internal constructor(private var spawn: NpcSpawnDef) {
    var attack: Int
        get() = spawn.attack
        set(value) { spawn = spawn.copy(attack = value) }
    var defence: Int
        get() = spawn.defence
        set(value) { spawn = spawn.copy(defence = value) }
    var strength: Int
        get() = spawn.strength
        set(value) { spawn = spawn.copy(strength = value) }
    var hitpoints: Int
        get() = spawn.hitpoints
        set(value) { spawn = spawn.copy(hitpoints = value.takeIf { it > 0 } ?: spawn.hitpoints) }
    var ranged: Int
        get() = spawn.ranged
        set(value) { spawn = spawn.copy(ranged = value) }
    var magic: Int
        get() = spawn.magic
        set(value) { spawn = spawn.copy(magic = value) }
    var respawnTicks: Int
        get() = spawn.respawnTicks
        set(value) { spawn = spawn.copy(respawnTicks = value.takeIf { it > 0 } ?: spawn.respawnTicks) }
    var attackAnimation: Int
        get() = spawn.attackAnimation
        set(value) { spawn = spawn.copy(attackAnimation = value) }
    var deathAnimation: Int
        get() = spawn.deathAnimation
        set(value) { spawn = spawn.copy(deathAnimation = value) }

    fun stats(
        attack: Int? = null,
        defence: Int? = null,
        strength: Int? = null,
        hitpoints: Int? = null,
        ranged: Int? = null,
        magic: Int? = null,
    ) {
        spawn = spawn.copy(
            attack = attack ?: spawn.attack,
            defence = defence ?: spawn.defence,
            strength = strength ?: spawn.strength,
            hitpoints = hitpoints?.takeIf { it > 0 } ?: spawn.hitpoints,
            ranged = ranged ?: spawn.ranged,
            magic = magic ?: spawn.magic,
        )
    }

    fun respawn(ticks: Int) {
        spawn = spawn.copy(respawnTicks = ticks.takeIf { it > 0 } ?: spawn.respawnTicks)
    }

    fun animations(attack: Int? = null, death: Int? = null) {
        spawn = spawn.copy(
            attackAnimation = attack ?: spawn.attackAnimation,
            deathAnimation = death ?: spawn.deathAnimation,
        )
    }

    fun movement(walkRadius: Int? = null, attackRange: Int? = null, alwaysActive: Boolean? = null) {
        spawn = spawn.copy(
            walkRadius = walkRadius ?: spawn.walkRadius,
            attackRange = attackRange ?: spawn.attackRange,
            alwaysActive = alwaysActive ?: spawn.alwaysActive,
        )
    }

    fun profile(value: String) {
        spawn = spawn.copy(profile = value.trim().takeIf { it.isNotEmpty() })
    }

    fun profile(value: NpcProfile) {
        spawn = spawn.copy(profile = value.key)
    }

    fun visibleWhen(condition: (Client) -> Boolean) {
        spawn = spawn.copy(condition = condition)
    }

    internal fun build(): NpcSpawnDef = spawn
}
