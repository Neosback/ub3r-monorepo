package net.dodian.uber.game.content.npcs

import net.dodian.uber.game.npc.toContentDefinition as toCanonicalContentDefinition

@Deprecated("Use net.dodian.uber.game.npc.NpcOptionSlot")
typealias NpcOptionSlot = net.dodian.uber.game.npc.NpcOptionSlot

@Deprecated("Use net.dodian.uber.game.npc.NpcOptionBinding")
typealias NpcOptionBinding = net.dodian.uber.game.npc.NpcOptionBinding

@Deprecated("Use net.dodian.uber.game.npc.NpcPluginDefinition")
typealias NpcPluginDefinition = net.dodian.uber.game.npc.NpcPluginDefinition

@Deprecated("Use net.dodian.uber.game.npc.NpcPluginContext")
typealias NpcPluginContext = net.dodian.uber.game.npc.NpcPluginContext

@Deprecated("Use net.dodian.uber.game.npc.NpcPluginStateStore")
typealias NpcPluginStateStore = net.dodian.uber.game.npc.NpcPluginStateStore

@Deprecated("Use net.dodian.uber.game.npc.toContentDefinition")
fun NpcPluginDefinition.toContentDefinition(
    explicitName: String,
    ownsSpawnDefinitionsFlag: Boolean,
): NpcContentDefinition = this.toCanonicalContentDefinition(explicitName, ownsSpawnDefinitionsFlag)
