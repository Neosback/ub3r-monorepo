package net.dodian.uber.game.api.plugin.skills

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

class SkillObjectInteraction(val player: SkillPlayer, val option: Int, val objectId: Int, val position: Position, val definition: GameObjectData?) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Int = objectId
    internal operator fun component3(): Position = position
    internal operator fun component4(): GameObjectData? = definition
}

class SkillNpcInteraction(val player: SkillPlayer, val option: Int, val npc: Npc) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Npc = npc
}

class SkillItemOnItemInteraction(val player: SkillPlayer, val itemUsed: Int, val otherItem: Int) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Int = itemUsed
    internal operator fun component3(): Int = otherItem
}

class SkillItemInteraction(val player: SkillPlayer, val option: Int, val itemId: Int, val itemSlot: Int, val interfaceId: Int) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Int = itemId
    internal operator fun component3(): Int = itemSlot
    internal operator fun component4(): Int = interfaceId
}

class SkillItemOnObjectInteraction(val player: SkillPlayer, val objectId: Int, val position: Position, val definition: GameObjectData?, val itemId: Int, val itemSlot: Int, val interfaceId: Int) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Int = objectId
    internal operator fun component3(): Position = position
    internal operator fun component4(): GameObjectData? = definition
    internal operator fun component5(): Int = itemId
    internal operator fun component6(): Int = itemSlot
    internal operator fun component7(): Int = interfaceId
}

class SkillMagicOnObjectInteraction(val player: SkillPlayer, val objectId: Int, val position: Position, val definition: GameObjectData?, val spellId: Int) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Int = objectId
    internal operator fun component3(): Position = position
    internal operator fun component4(): GameObjectData? = definition
    internal operator fun component5(): Int = spellId
}

class SkillButtonInteraction(val player: SkillPlayer, val rawButtonId: Int, val opIndex: Int, val activeInterfaceId: Int) {
    internal operator fun component1(): Client = player.protocolClient()
    internal operator fun component2(): Int = rawButtonId
    internal operator fun component3(): Int = opIndex
}
