package net.dodian.uber.game.npc

import java.util.Collections
import java.util.WeakHashMap
import net.dodian.uber.game.engine.systems.cache.CacheVarbitDefinition
import net.dodian.uber.game.model.entity.player.Client

object NpcClientConfigService {
    private val varbitDefinitions = LinkedHashMap<Int, CacheVarbitDefinition>()
    private val clientVarps = Collections.synchronizedMap(WeakHashMap<Client, MutableMap<Int, Int>>())

    fun initialize(varbits: Map<Int, CacheVarbitDefinition>) {
        synchronized(varbitDefinitions) {
            varbitDefinitions.clear()
            varbitDefinitions.putAll(varbits)
        }
    }

    fun varbitDefinition(id: Int): CacheVarbitDefinition? =
        synchronized(varbitDefinitions) { varbitDefinitions[id] }

    fun setVarp(client: Client, id: Int, value: Int) {
        rememberVarp(client, id, value)
        client.setVarp(id, value)
    }

    fun setVarbit(client: Client, id: Int, value: Int): Boolean {
        val definition = varbitDefinition(id) ?: return false
        require(value in 0..definition.maxValue) {
            "Varbit $id value $value is outside 0..${definition.maxValue}"
        }
        val current = currentVarp(client, definition.varp)
        val mask = definition.maxValue shl definition.leastSignificantBit
        val next = (current and mask.inv()) or (value shl definition.leastSignificantBit)
        setVarp(client, definition.varp, next)
        return true
    }

    fun clearVarbit(client: Client, id: Int): Boolean = setVarbit(client, id, 0)

    fun clearForTests() {
        clientVarps.clear()
    }

    internal fun currentVarpForTests(client: Client, id: Int): Int = currentVarp(client, id)

    private fun currentVarp(client: Client, id: Int): Int =
        synchronized(clientVarps) { clientVarps[client]?.get(id) ?: 0 }

    private fun rememberVarp(client: Client, id: Int, value: Int) {
        synchronized(clientVarps) {
            clientVarps.getOrPut(client) { LinkedHashMap() }[id] = value
        }
    }
}
