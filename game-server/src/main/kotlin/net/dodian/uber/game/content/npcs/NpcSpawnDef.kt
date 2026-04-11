package net.dodian.uber.game.content.npcs

@Deprecated("Use net.dodian.uber.game.npc.MYSQL_DEFAULT_STAT")
const val MYSQL_DEFAULT_STAT = net.dodian.uber.game.npc.MYSQL_DEFAULT_STAT

@Deprecated("Use net.dodian.uber.game.npc.NORTH")
const val NORTH = net.dodian.uber.game.npc.NORTH
@Deprecated("Use net.dodian.uber.game.npc.EAST")
const val EAST = net.dodian.uber.game.npc.EAST
@Deprecated("Use net.dodian.uber.game.npc.SOUTH")
const val SOUTH = net.dodian.uber.game.npc.SOUTH
@Deprecated("Use net.dodian.uber.game.npc.WEST")
const val WEST = net.dodian.uber.game.npc.WEST
@Deprecated("Use net.dodian.uber.game.npc.north")
const val north = net.dodian.uber.game.npc.north
@Deprecated("Use net.dodian.uber.game.npc.east")
const val east = net.dodian.uber.game.npc.east
@Deprecated("Use net.dodian.uber.game.npc.south")
const val south = net.dodian.uber.game.npc.south
@Deprecated("Use net.dodian.uber.game.npc.west")
const val west = net.dodian.uber.game.npc.west

@Deprecated("Use net.dodian.uber.game.npc.NpcSpawnDef")
typealias NpcSpawnDef = net.dodian.uber.game.npc.NpcSpawnDef

@Deprecated("Use net.dodian.uber.game.npc.NpcSpawnPoint")
typealias NpcSpawnPoint = net.dodian.uber.game.npc.NpcSpawnPoint

@Deprecated("Use net.dodian.uber.game.npc.point")
fun point(x: Int, y: Int, z: Int = 0, face: Int = 0): NpcSpawnPoint =
    net.dodian.uber.game.npc.point(x = x, y = y, z = z, face = face)

@Deprecated("Use net.dodian.uber.game.npc.spawnEntries")
fun spawnEntries(
    npcId: Int,
    vararg points: NpcSpawnPoint,
): List<NpcSpawnDef> = net.dodian.uber.game.npc.spawnEntries(npcId, *points)
