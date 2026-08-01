package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import org.slf4j.LoggerFactory

object NpcClientMorphService {
    private val logger = LoggerFactory.getLogger(NpcClientMorphService::class.java)
    private val definitions = LinkedHashMap<Int, CacheNpcDefinition>()

    fun initialize(rawDefinitions: Map<Int, CacheNpcDefinition>) {
        synchronized(definitions) {
            definitions.clear()
            definitions.putAll(rawDefinitions)
        }
    }

    fun setMorphIndex(client: Client, npc: Npc, index: Int): Boolean {
        return setMorphIndex(client, npc.id, index)
    }

    internal fun setMorphIndex(client: Client, npcId: Int, index: Int): Boolean {
        val definition = definition(npcId)
        if (definition == null || !definition.hasClientMorph()) {
            logger.info("NPC {} has no client-cache morph metadata; cannot set morph index {}", npcId, index)
            return false
        }
        val childId = definition.transformChildren.getOrNull(index)
        if (childId == null || childId < 0) {
            logger.info(
                "NPC {} cannot morph to child index {}. children={}",
                npcId,
                index,
                definition.transformChildren,
            )
            return false
        }
        return applyMorphValue(client, definition, index)
    }

    fun clearMorph(client: Client, npc: Npc): Boolean {
        val definition = definition(npc.id) ?: return false
        return applyMorphValue(client, definition, 0)
    }

    fun definition(id: Int): CacheNpcDefinition? =
        synchronized(definitions) { definitions[id] }

    private fun applyMorphValue(client: Client, definition: CacheNpcDefinition, value: Int): Boolean {
        if (definition.transformVarbit >= 0) {
            return NpcClientConfigService.setVarbit(client, definition.transformVarbit, value)
        }
        if (definition.transformVarp >= 0) {
            NpcClientConfigService.setVarp(client, definition.transformVarp, value)
            return true
        }
        return false
    }
}

fun CacheNpcDefinition.hasClientMorph(): Boolean =
    transformChildren.isNotEmpty() && (transformVarbit >= 0 || transformVarp >= 0)
