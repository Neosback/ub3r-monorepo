package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc

fun interface NpcAttackHandler {
    fun handleAttack(npc: Npc): Boolean
}
