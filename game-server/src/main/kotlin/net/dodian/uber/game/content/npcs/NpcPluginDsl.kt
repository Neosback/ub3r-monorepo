package net.dodian.uber.game.content.npcs

@Deprecated("Use net.dodian.uber.game.npc.NpcClickHandlerWithState")
typealias NpcClickHandlerWithState = net.dodian.uber.game.npc.NpcClickHandlerWithState

@Deprecated("Use net.dodian.uber.game.npc.NpcOptionsBuilder")
typealias NpcOptionsBuilder = net.dodian.uber.game.npc.NpcOptionsBuilder

@Deprecated("Use net.dodian.uber.game.npc.NpcSpawnsBuilder")
typealias NpcSpawnsBuilder = net.dodian.uber.game.npc.NpcSpawnsBuilder

@Deprecated("Use net.dodian.uber.game.npc.NpcPluginBuilder")
typealias NpcPluginBuilder = net.dodian.uber.game.npc.NpcPluginBuilder

@Deprecated("Use net.dodian.uber.game.npc.npcPlugin")
fun npcPlugin(name: String, init: NpcPluginBuilder.() -> Unit): NpcPluginDefinition =
    net.dodian.uber.game.npc.npcPlugin(name, init)
