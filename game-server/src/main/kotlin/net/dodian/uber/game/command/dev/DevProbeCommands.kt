package net.dodian.uber.game.command.dev

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditStore
import net.dodian.uber.game.engine.systems.interaction.commands.CommandContent
import net.dodian.uber.game.engine.systems.interaction.commands.CommandContext
import net.dodian.uber.game.engine.systems.interaction.commands.commands
import net.dodian.uber.game.engine.routing.WorldRouteService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.objects.DoorRegistry

object DevProbeCommands : CommandContent {
    override fun definitions() =
        commands {
            command("near", "nearobj", "neardoor") {
                handleNear(this)
            }
            command("los") {
                handleLineOfSight(this)
            }
        }
}

private fun handleLineOfSight(context: CommandContext): Boolean {
    if (!context.specialRights) return false
    val client = context.client
    val target = client.target
    if (target == null) {
        context.reply("No combat target selected.")
        return true
    }
    val trace = WorldRouteService.rayCast(client.position, target.position, lineOfSight = true)
    val targetLabel = if (target is Npc) "npc:${target.id}/${target.slot}" else "player:${target.slot}"
    val message = if (trace.success) "LOS clear to $targetLabel tiles=${trace.coordinates.size}" else "LOS blocked to $targetLabel tiles=${trace.coordinates.size}"
    context.reply(message)
    return true
}

private fun handleNear(context: CommandContext): Boolean {
    val client = context.client
    if (!context.specialRights) return false
    val pos = client.position
    val radius = 10
    val doorsOnly = context.alias == "neardoor"
    val objectsOnly = context.alias == "nearobj"

    val results = mutableListOf<ProbeEntry>()

    if (!objectsOnly) {
        for (i in DoorRegistry.doorId.indices) {
            val doorId = DoorRegistry.doorId[i]
            val dx = DoorRegistry.doorX[i]
            val dy = DoorRegistry.doorY[i]
            val dz = DoorRegistry.doorHeight[i]
            if (doorId <= 0 || dx <= 0 || dy <= 0 || dz != pos.z) continue
            val dist = Math.max(Math.abs(pos.x - dx), Math.abs(pos.y - dy))
            if (dist <= radius) {
                val state = if (DoorRegistry.doorState[i] == 1) "open" else "closed"
                val face = DoorRegistry.doorFace[i]
                results += ProbeEntry(
                    dist, dx, dy, dz, doorId,
                    label = "door id=$doorId face=$face $state"
                )
            }
        }
    }

    if (!doorsOnly) {
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val x = pos.x + dx
                val y = pos.y + dy
                val dist = Math.max(Math.abs(dx), Math.abs(dy))
                if (dist == 0) continue

                val flags = WorldRouteService.getFlags(x, y, pos.z)
                val hasBlocked = flags and 0x200000 != 0

                val objects = CacheCollisionAuditStore.objectsForTile(x, y)
                for (obj in objects) {
                    if (obj.skipped || obj.plane != pos.z) continue
                    val def = GameObjectData.forId(obj.objectId)
                    val solid = if (def.isSolid()) "solid" else "passable"
                    results += ProbeEntry(
                        dist, x, y, pos.z, obj.objectId,
                        label = "obj id=${obj.objectId} type=${obj.type} rot=${obj.rotation} $solid"
                    )
                }

                if (objects.isEmpty() && hasBlocked) {
                    results += ProbeEntry(
                        dist, x, y, pos.z, 0,
                        label = "BLOCKED (flags=0x${flags.toString(16)})"
                    )
                }
            }
        }
    }

    if (results.isEmpty()) {
        context.reply("Nothing found within $radius tiles.")
        return true
    }

    results.sortBy { it.dist }
    val lines = results.take(40).joinToString("\n") { e ->
        "(${e.x},${e.y}) d=${e.dist} ${e.label}"
    }
    val header = "--- ${results.size} entries within ${radius}t (showing ${minOf(40, results.size)}) ---"
    println(header)
    println(lines)
    context.reply("$header\n$lines")
    return true
}

private data class ProbeEntry(
    val dist: Int,
    val x: Int, val y: Int, val z: Int,
    val id: Int,
    val label: String,
)
