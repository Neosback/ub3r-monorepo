package net.dodian.uber.game.npc.dsl

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.npc.NpcDefinitionOverrideBuilder
import net.dodian.uber.game.npc.NpcPluginBuilder
import net.dodian.uber.game.npc.NpcSequentialOptionBuilder
import net.dodian.uber.game.npc.NpcScript
import net.dodian.uber.game.npc.NpcSpawnDef

class NpcDefinitionDsl(
    private val script: NpcScript,
    private val plugin: NpcPluginBuilder
) {
    fun stats(block: NpcDefinitionOverrideBuilder.() -> Unit) {
        plugin.npc(script.primaryId, block)
    }

    fun spawns(vararg entries: NpcSpawnDef) {
        script.spawns.addAll(entries)
    }

    fun spawns(entries: List<NpcSpawnDef>) {
        script.spawns.addAll(entries)
    }

    fun onTalkTo(block: NpcSequentialOptionBuilder.() -> Unit) {
        plugin.options {
            talkTo("talk-to", block)
        }
    }

    fun onTrade(action: NpcAction) {
        plugin.options {
            second("trade") { client, npc ->
                val context = NpcActionContext(client, npc, "trade")
                action(context)
                true
            }
        }
    }

    fun onOption(name: String, action: NpcOptionDsl.() -> Unit) {
        plugin.options {
            val dsl = NpcOptionDsl()
            dsl.action()
            val handler = dsl.build()
            when (name.lowercase()) {
                "talk-to", "talk to", "first" -> first(name, handler)
                "trade", "second" -> second(name, handler)
                "teleport", "third" -> third(name, handler)
                "fourth" -> fourth(name, handler)
                else -> third(name, handler)
            }
        }
    }
}

class NpcOptionDsl {
    private var actionBlock: ((Client, Npc) -> Boolean)? = null

    fun branch(block: BranchDsl.() -> Unit) {
        actionBlock = BranchDsl().apply(block).build()
    }

    fun action(block: NpcAction) {
        actionBlock = { client, npc ->
            val context = NpcActionContext(client, npc, "option")
            block(context)
            true
        }
    }

    internal fun build(): (Client, Npc) -> Boolean = actionBlock ?: { _, _ -> false }
}
