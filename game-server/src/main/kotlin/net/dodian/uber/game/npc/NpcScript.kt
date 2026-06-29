package net.dodian.uber.game.npc

import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.npc.dsl.NpcDefinitionDsl
import net.dodian.uber.game.npc.dsl.NpcProfile

abstract class NpcScript(
    val scriptName: String,
    val primaryId: Int,
    vararg alternateIds: Int
) : NpcModule, NpcSpawnSource {

    val npcIds: IntArray = intArrayOf(primaryId, *alternateIds)
    override val spawns = ArrayList<NpcSpawnDef>()

    protected fun define(
        block: NpcDefinitionDsl.() -> Unit = {}
    ): NpcContentDefinition {
        return npcPlugin(scriptName) {
            ids(*npcIds)
            val dsl = NpcDefinitionDsl(this@NpcScript, this)
            dsl.block()
        }.toContentDefinition(scriptName)
    }

    protected fun profile(suffix: String): NpcProfile {
        val key = if (suffix.contains(".")) suffix else "${scriptName.normalizedKey()}.$suffix"
        return NpcProfile(key)
    }

    protected fun pos(x: Int, y: Int, z: Int = 0): Position {
        return Position(x, y, z)
    }

    protected fun spawn(
        x: Int,
        y: Int,
        z: Int = 0,
        face: Int = NORTH,
        walkRadius: Int = 0,
        profile: NpcProfile? = null
    ): NpcSpawnDef {
        return NpcSpawnDef(
            npcId = primaryId,
            x = x,
            y = y,
            z = z,
            face = face,
            walkRadius = walkRadius,
            profile = profile?.key
        )
    }

    protected fun template(
        npcId: Int = primaryId,
        hitpoints: Int = CACHE_DEFAULT_STAT,
        attack: Int = CACHE_DEFAULT_STAT,
        defence: Int = CACHE_DEFAULT_STAT,
        strength: Int = CACHE_DEFAULT_STAT,
        magic: Int = CACHE_DEFAULT_STAT,
        ranged: Int = CACHE_DEFAULT_STAT,
        face: Int = NORTH,
        walkRadius: Int = 0,
        profile: NpcProfile? = null
    ): NpcSpawnTemplate {
        return NpcSpawnTemplate(
            npcId = npcId,
            hitpoints = hitpoints,
            attack = attack,
            defence = defence,
            strength = strength,
            magic = magic,
            ranged = ranged,
            face = face,
            walkRadius = walkRadius,
            profile = profile?.key
        )
    }

    var deathHandler: ((Npc, Client?) -> Unit)? = null

    protected fun onDeath(block: (Npc, Client?) -> Unit) {
        deathHandler = block
    }

    private fun String.normalizedKey(): String {
        return lowercase()
            .replace(" ", "_")
            .replace("-", "_")
    }
}
