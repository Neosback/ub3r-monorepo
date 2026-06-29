package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.player.Client

const val CACHE_DEFAULT_STAT = -1
const val MYSQL_DEFAULT_STAT = CACHE_DEFAULT_STAT
const val NORTH = 0
const val EAST = 2
const val SOUTH = 4
const val WEST = 6
const val north = NORTH
const val east = EAST
const val south = SOUTH
const val west = WEST

interface NpcSpawnSource {
    val spawns: List<NpcSpawnDef>
}

data class NpcSpawnDef(
    val npcId: Int,
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val face: Int = 0,
    val rx: Int = 0,
    val ry: Int = 0,
    val rx2: Int = 0,
    val ry2: Int = 0,
    val moveChance: Int = 0,
    val live: Boolean = true,
    val walkRadius: Int = 0,
    val attackRange: Int = 6,
    val alwaysActive: Boolean = false,
    val condition: (Client) -> Boolean = { true },
    val respawnTicks: Int = MYSQL_DEFAULT_STAT,
    val attack: Int = MYSQL_DEFAULT_STAT,
    val defence: Int = MYSQL_DEFAULT_STAT,
    val strength: Int = MYSQL_DEFAULT_STAT,
    val hitpoints: Int = MYSQL_DEFAULT_STAT,
    val ranged: Int = MYSQL_DEFAULT_STAT,
    val magic: Int = MYSQL_DEFAULT_STAT,
    val attackAnimation: Int = MYSQL_DEFAULT_STAT,
    val deathAnimation: Int = MYSQL_DEFAULT_STAT,
    val profile: String? = null,
) {
    fun withStatOverrides(
        respawnTicks: Int? = null,
        attack: Int? = null,
        defence: Int? = null,
        strength: Int? = null,
        hitpoints: Int? = null,
        ranged: Int? = null,
        magic: Int? = null,
    ): NpcSpawnDef = copy(
        respawnTicks = respawnTicks ?: this.respawnTicks,
        attack = attack ?: this.attack,
        defence = defence ?: this.defence,
        strength = strength ?: this.strength,
        hitpoints = hitpoints ?: this.hitpoints,
        ranged = ranged ?: this.ranged,
        magic = magic ?: this.magic,
    )
}

data class NpcSpawnPoint(
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val face: Int = 0,
)

fun point(x: Int, y: Int, z: Int = 0, face: Int = 0): NpcSpawnPoint = NpcSpawnPoint(x = x, y = y, z = z, face = face)

fun spawnEntries(
    npcId: Int,
    vararg points: NpcSpawnPoint,
): List<NpcSpawnDef> = points.map { p -> NpcSpawnDef(npcId = npcId, x = p.x, y = p.y, z = p.z, face = p.face) }

data class NpcSpawnTemplate(
    val npcId: Int,
    val face: Int = 0,
    val rx: Int = 0,
    val ry: Int = 0,
    val rx2: Int = 0,
    val ry2: Int = 0,
    val moveChance: Int = 0,
    val live: Boolean = true,
    val walkRadius: Int = 0,
    val attackRange: Int = 6,
    val alwaysActive: Boolean = false,
    val condition: (Client) -> Boolean = { true },
    val respawnTicks: Int = CACHE_DEFAULT_STAT,
    val attack: Int = CACHE_DEFAULT_STAT,
    val defence: Int = CACHE_DEFAULT_STAT,
    val strength: Int = CACHE_DEFAULT_STAT,
    val hitpoints: Int = CACHE_DEFAULT_STAT,
    val ranged: Int = CACHE_DEFAULT_STAT,
    val magic: Int = CACHE_DEFAULT_STAT,
    val attackAnimation: Int = CACHE_DEFAULT_STAT,
    val deathAnimation: Int = CACHE_DEFAULT_STAT,
    val profile: String? = null,
) {
    fun at(
        x: Int,
        y: Int,
        z: Int = 0,
        face: Int = this.face,
        walkRadius: Int = this.walkRadius,
        profile: String? = this.profile
    ): NpcSpawnDef = NpcSpawnDef(
        npcId = npcId,
        x = x,
        y = y,
        z = z,
        face = face,
        rx = rx,
        ry = ry,
        rx2 = rx2,
        ry2 = ry2,
        moveChance = moveChance,
        live = live,
        walkRadius = walkRadius,
        attackRange = attackRange,
        alwaysActive = alwaysActive,
        condition = condition,
        respawnTicks = respawnTicks,
        attack = attack,
        defence = defence,
        strength = strength,
        hitpoints = hitpoints,
        ranged = ranged,
        magic = magic,
        attackAnimation = attackAnimation,
        deathAnimation = deathAnimation,
        profile = profile
    )
}

fun spawnTemplate(
    npcId: Int,
    face: Int = 0,
    rx: Int = 0,
    ry: Int = 0,
    rx2: Int = 0,
    ry2: Int = 0,
    moveChance: Int = 0,
    live: Boolean = true,
    walkRadius: Int = 0,
    attackRange: Int = 6,
    alwaysActive: Boolean = false,
    condition: (Client) -> Boolean = { true },
    respawnTicks: Int = CACHE_DEFAULT_STAT,
    attack: Int = CACHE_DEFAULT_STAT,
    defence: Int = CACHE_DEFAULT_STAT,
    strength: Int = CACHE_DEFAULT_STAT,
    hitpoints: Int = CACHE_DEFAULT_STAT,
    ranged: Int = CACHE_DEFAULT_STAT,
    magic: Int = CACHE_DEFAULT_STAT,
    attackAnimation: Int = CACHE_DEFAULT_STAT,
    deathAnimation: Int = CACHE_DEFAULT_STAT,
    profile: String? = null,
): NpcSpawnTemplate = NpcSpawnTemplate(
    npcId = npcId,
    face = face,
    rx = rx,
    ry = ry,
    rx2 = rx2,
    ry2 = ry2,
    moveChance = moveChance,
    live = live,
    walkRadius = walkRadius,
    attackRange = attackRange,
    alwaysActive = alwaysActive,
    condition = condition,
    respawnTicks = respawnTicks,
    attack = attack,
    defence = defence,
    strength = strength,
    hitpoints = hitpoints,
    ranged = ranged,
    magic = magic,
    attackAnimation = attackAnimation,
    deathAnimation = deathAnimation,
    profile = profile
)
