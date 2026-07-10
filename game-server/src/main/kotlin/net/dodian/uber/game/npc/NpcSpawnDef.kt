package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.player.Client

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
    val attackRange: Int = 0,
    val leashDistance: Int = 15,
    val alwaysActive: Boolean = false,
    val condition: (Client) -> Boolean = { true },
    val overrides: NpcServerPatch = NpcServerPatch(),
    val profile: String? = null,
) {
    val respawnTicks: Int? get() = overrides.respawnTicks
    val attack: Int? get() = overrides.attack
    val defence: Int? get() = overrides.defence
    val strength: Int? get() = overrides.strength
    val hitpoints: Int? get() = overrides.hitpoints
    val ranged: Int? get() = overrides.ranged
    val magic: Int? get() = overrides.magic
    val attackAnimation: Int? get() = overrides.attackAnimation
    val deathAnimation: Int? get() = overrides.deathAnimation
    val headIcon: Int? get() = overrides.headIcon
    val transformTo: Int? get() = overrides.transformTo
}

class NpcSpawnOverrideBuilder internal constructor(private var spawn: NpcSpawnDef) {
    private val server = NpcServerDefinitionBuilder()
    var attack: Int?
        get() = spawn.overrides.attack
        set(value) { value?.let { server.attack = it } }
    var defence: Int?
        get() = spawn.overrides.defence
        set(value) { value?.let { server.defence = it } }
    var strength: Int?
        get() = spawn.overrides.strength
        set(value) { value?.let { server.strength = it } }
    var hitpoints: Int?
        get() = spawn.overrides.hitpoints
        set(value) { value?.let { server.hitpoints = it } }
    var ranged: Int?
        get() = spawn.overrides.ranged
        set(value) { value?.let { server.ranged = it } }
    var magic: Int?
        get() = spawn.overrides.magic
        set(value) { value?.let { server.magic = it } }
    var respawnTicks: Int?
        get() = spawn.overrides.respawnTicks
        set(value) { value?.let { server.respawnTicks = it } }
    var attackAnimation: Int?
        get() = spawn.overrides.attackAnimation
        set(value) { value?.let { server.attackAnimation = it } }
    var defenceAnimation: Int?
        get() = spawn.overrides.defenceAnimation
        set(value) { value?.let { server.defenceAnimation = it } }
    var deathAnimation: Int?
        get() = spawn.overrides.deathAnimation
        set(value) { value?.let { server.deathAnimation = it } }
    var headIcon: Int?
        get() = spawn.overrides.headIcon
        set(value) { value?.let { server.headIcon = it } }
    var transformTo: Int?
        get() = spawn.overrides.transformTo
        set(value) { value?.let { server.transformTo = it } }

    fun stats(
        attack: Int? = null,
        defence: Int? = null,
        strength: Int? = null,
        hitpoints: Int? = null,
        ranged: Int? = null,
        magic: Int? = null,
    ) {
        server.stats(
            attack = attack,
            defence = defence,
            strength = strength,
            hitpoints = hitpoints,
            ranged = ranged,
            magic = magic,
        )
    }

    fun respawn(ticks: Int) {
        server.respawn(ticks)
    }

    fun animations(attack: Int? = null, defence: Int? = null, death: Int? = null) {
        server.animations(attack = attack, defence = defence, death = death)
    }

    fun overrides(block: NpcServerDefinitionBuilder.() -> Unit) {
        server.apply(block)
    }

    fun server(block: NpcServerDefinitionBuilder.() -> Unit) {
        overrides(block)
    }

    fun display(headIcon: Int? = null, transformTo: Int? = null, block: NpcDisplayOverrideBuilder.() -> Unit = {}) {
        val display = NpcDisplayOverrideBuilder().apply {
            this.headIcon = headIcon
            this.transformTo = transformTo
            block()
        }.build()
        display.headIcon?.let { server.headIcon = it }
        display.transformTo?.let { server.transformTo = it }
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

    internal fun build(): NpcSpawnDef = spawn.copy(overrides = spawn.overrides.overlay(server.buildPatch()))
}

class NpcDisplayOverrideBuilder internal constructor() {
    var headIcon: Int? = null
    var transformTo: Int? = null

    internal fun build(): NpcDisplayOverride =
        NpcDisplayOverride(
            headIcon = headIcon?.takeIf { it >= 0 },
            transformTo = transformTo?.takeIf { it >= 0 },
        )
}
