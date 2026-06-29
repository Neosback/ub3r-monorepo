package net.dodian.uber.game.npc

import net.dodian.uber.game.api.plugin.ContentBootstrap
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.events.combat.NpcDeathEvent

object NpcScriptBootstrap : ContentBootstrap {
    override val id: String = "NpcScriptBootstrap"

    override fun bootstrap() {
        GameEventBus.on<NpcDeathEvent> { event ->
            val script = ContentModuleIndex.npcModules
                .filterIsInstance<NpcScript>()
                .firstOrNull { event.npc.id in it.npcIds }
            script?.deathHandler?.invoke(event.npc, event.killer)
            true
        }
    }
}
