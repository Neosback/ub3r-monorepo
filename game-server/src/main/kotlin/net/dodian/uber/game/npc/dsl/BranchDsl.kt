package net.dodian.uber.game.npc.dsl

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

class BranchDsl {
    private val cases = mutableListOf<BranchCase>()
    private var fallbackAction: NpcAction? = null

    fun onProfile(profile: NpcProfile, action: NpcAction) {
        cases += BranchCase(
            predicate = { this.profile == profile.key },
            action = action
        )
    }

    fun onCondition(predicate: NpcActionContext.() -> Boolean): ConditionClause {
        return ConditionClause(predicate)
    }

    infix fun ConditionClause.then(action: NpcAction) {
        cases += BranchCase(predicate, action)
    }

    fun fallback(action: NpcAction) {
        fallbackAction = action
    }

    fun build(): (Client, Npc) -> Boolean {
        return { client, npc ->
            val context = NpcActionContext(client, npc, "teleport")
            val match = cases.firstOrNull { it.predicate(context) }
            if (match != null) {
                match.action(context)
            } else if (fallbackAction != null) {
                fallbackAction!!.invoke(context)
            }
            true
        }
    }

    class ConditionClause(val predicate: NpcActionContext.() -> Boolean)

    private data class BranchCase(
        val predicate: NpcActionContext.() -> Boolean,
        val action: NpcAction
    )
}
