package net.dodian.uber.game.npc

import java.util.concurrent.ConcurrentHashMap
import net.dodian.uber.game.model.entity.Entity
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

data class NpcDefenceResult(
    val damage: Int,
    val suppressDefaultReaction: Boolean = false,
)

class NpcDefenceContext internal constructor(
    val npc: Npc,
    val attacker: Client,
    damage: Int,
    val hitType: Entity.hitType,
) {
    var damage: Int = damage.coerceAtLeast(0)
    var suppressDefaultReaction: Boolean = false
}

data class NpcCombatProfile(
    val attack: NpcAttackHandler,
    val defend: ((NpcDefenceContext) -> Unit)? = null,
)

/** Registration, validation, and dispatch only; behavior stays in each NPC/family file. */
object NpcCombatRegistry {
    private data class Registration(val owner: String, val profile: NpcCombatProfile)
    private val registrations = ConcurrentHashMap<Int, Registration>()

    fun registerFamily(owner: String, npcIds: IntArray, profile: NpcCombatProfile) {
        require(owner.isNotBlank()) { "NPC combat owner cannot be blank" }
        require(npcIds.isNotEmpty()) { "NPC combat family $owner has no ids" }
        npcIds.distinct().forEach { npcId ->
            require(npcId >= 0) { "NPC combat id must be non-negative: $npcId" }
            val registration = Registration(owner, profile)
            val existing = registrations.putIfAbsent(npcId, registration)
            require(existing == null || existing == registration) {
                "Duplicate NPC combat registration id=$npcId owner=$owner existing=${existing?.owner}"
            }
        }
    }

    @JvmStatic
    fun handleAttack(npc: Npc): Boolean {
        val attack = registrations[npc.id]?.profile?.attack ?: return false
        attack.handleAttack(npc)
        return true
    }

    @JvmStatic
    fun applyDefence(npc: Npc, attacker: Client, damage: Int, hitType: Entity.hitType): NpcDefenceResult {
        val hook = registrations[npc.id]?.profile?.defend ?: return NpcDefenceResult(damage.coerceAtLeast(0))
        val context = NpcDefenceContext(npc, attacker, damage, hitType)
        hook(context)
        return NpcDefenceResult(context.damage.coerceAtLeast(0), context.suppressDefaultReaction)
    }

    fun ownerOf(npcId: Int): String? = registrations[npcId]?.owner

    internal fun clearForTests() = registrations.clear()
}
