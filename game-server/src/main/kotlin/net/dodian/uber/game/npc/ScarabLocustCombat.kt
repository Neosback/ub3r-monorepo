package net.dodian.uber.game.npc

import kotlin.math.floor
import net.dodian.uber.game.engine.util.Utils
import net.dodian.uber.game.model.entity.Entity
import net.dodian.uber.game.model.entity.npc.Npc

/**
 * Legacy-equivalent attacks for the Sophanem scarab/locust families.  The
 * literal graphics and animations are content values, kept here beside their
 * owning families rather than in Npc.specialCondition's global switch.
 */
internal object ScarabLocustCombat : NpcAttackHandler {
    override fun handleAttack(npc: Npc): Boolean {
        val target = npc.getTarget(true) ?: return false
        npc.setFocus(target.position.x, target.position.y)
        val halfHealth = npc.currentHealth <= npc.maxHealth / 2

        when (npc.id) {
            794, 799 -> scarabMage(npc, target, halfHealth)
            795, 800 -> locustMelee(npc, target, halfHealth)
            796, 801 -> locustRanged(npc, target, halfHealth)
            else -> return false
        }
        return true
    }

    private fun scarabMage(npc: Npc, target: net.dodian.uber.game.model.entity.player.Client, halfHealth: Boolean) {
        npc.lastAttack = npc.attackTimer
        if (!halfHealth) {
            val hit = Utils.random(floor(npc.maxHit * npc.magic).toInt())
            npc.sendArrow(target, 87, 88)
            npc.delayGfx(target, 708, 89, npc.getDistanceDelay(target.distanceToPoint(npc.position, target.position), true), hit, false, npc, Entity.damageType.MAGIC)
        } else {
            val hit = if (npc.landHit(target, true)) Utils.random(npc.maxHit) else 0
            npc.performAnimation(npc.data.attackEmote, 0)
            target.dealDamage(hit, Entity.hitType.STANDARD, npc, Entity.damageType.MELEE)
        }
    }

    private fun locustMelee(npc: Npc, target: net.dodian.uber.game.model.entity.player.Client, halfHealth: Boolean) {
        npc.lastAttack = npc.attackTimer
        if (!halfHealth) {
            val hit = if (npc.landHit(target, true)) Utils.random(npc.maxHit) else 0
            npc.performAnimation(npc.data.attackEmote, 0)
            target.dealDamage(hit, Entity.hitType.STANDARD, npc, Entity.damageType.MELEE)
        } else {
            npc.CalculateMaxHit(false)
            val hit = if (npc.landHit(target, false)) Utils.random(npc.maxHit) else 0
            npc.sendArrow(target, -1, 276)
            npc.delayGfx(target, 5446, -1, npc.getDistanceDelay(target.distanceToPoint(npc.position, target.position), false), hit, false, npc, Entity.damageType.RANGED)
        }
    }

    private fun locustRanged(npc: Npc, target: net.dodian.uber.game.model.entity.player.Client, halfHealth: Boolean) {
        npc.lastAttack = npc.attackTimer
        if (!halfHealth) {
            npc.CalculateMaxHit(false)
            val hit = if (npc.landHit(target, false)) Utils.random(npc.maxHit) else 0
            npc.sendArrow(target, 23, 14)
            npc.delayGfx(target, npc.data.attackEmote, -1, npc.getDistanceDelay(target.distanceToPoint(npc.position, target.position), false), hit, false, npc, Entity.damageType.RANGED)
        } else {
            val hit = Utils.random(floor(npc.maxHit * npc.magic).toInt())
            npc.sendArrow(target, -1, 146)
            npc.delayGfx(target, 5446, 147, npc.getDistanceDelay(target.distanceToPoint(npc.position, target.position), true), hit, false, npc, Entity.damageType.MAGIC)
        }
    }
}
