package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.event.GameEventScheduler
import net.dodian.utilities.Randoms
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt
import net.dodian.uber.game.model.entity.Entity
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.netty.listener.out.SendMessage

private const val MELEE_SPEED = 4
private const val BREATH_SPEED = 6

private const val FIRE_MAX_DMG = 65
private const val SPECIAL_MAX_DMG = 50
private const val MELEE_MAX_DMG = 25

private const val FREEZE_TICKS = 10

private const val ANIM_KBD_BREATH = 81
private const val ANIM_KBD_BLOCK = 90
private const val ANIM_KBD_MELEE = 91
private const val ANIM_KBD_DEATH = 92

private const val PROJ_DRAGONFIRE = 393
private const val PROJ_SHOCK = 395
private const val PROJ_ICE = 396
private const val PROJ_POISON = 394

private const val GFX_DRAGONFIRE_HIT = 81
private const val GFX_SHOCK_HIT = 84
private const val GFX_ICE_HIT = 82
private const val GFX_POISON_HIT = 83

internal object KingBlackDragon : NpcFamily by npcFamily("npc.king_black_dragon", 239, block = {
    definition {
        name = "King black dragon"
        examine = "Rawr xD UwU"
    }

    server {
        defenceAnimation = ANIM_KBD_BLOCK
        attackAnimation = ANIM_KBD_BREATH
        deathAnimation = ANIM_KBD_DEATH
        respawnTicks = 180
        attack = 210
        strength = 250
        defence = 200
        hitpoints = 330
        ranged = 320
        magic = 1200
        alwaysAggressive = true
    }

    combat {
        handler(KbdCombat)
    }

    spawns {
        spawn(3315, 9376)
    }
})

private enum class KbdAttack {
    MELEE,
    DRAGONFIRE,
    SHOCK,
    ICE,
    POISON,
}

private object KbdCombat : NpcAttackHandler {

    override fun handleAttack(npc: Npc): Boolean {
        val target = npc.getTarget(true) ?: return false
        val inMeleeRange = npc.gapDistanceTo(target) <= 1

        npc.setFocus(target.position.x, target.position.y)

        when (selectAttack(inMeleeRange)) {
            KbdAttack.MELEE -> attackMelee(npc, target)
            KbdAttack.DRAGONFIRE -> attackDragonfire(npc, target)
            KbdAttack.SHOCK -> attackShock(npc, target)
            KbdAttack.ICE -> attackIce(npc, target)
            KbdAttack.POISON -> attackPoison(npc, target)
        }
        return true
    }

    private fun selectAttack(inMeleeRange: Boolean): KbdAttack {
        val attacks = buildList {
            if (inMeleeRange) {
                repeat(2) { add(KbdAttack.MELEE) }
            }
            repeat(if (inMeleeRange) 3 else 5) {
                add(KbdAttack.DRAGONFIRE)
            }
            add(KbdAttack.SHOCK)
            add(KbdAttack.ICE)
            add(KbdAttack.POISON)
        }
        return attacks[Randoms.randomMinusOne(attacks.size)]
    }

    private fun attackMelee(npc: Npc, target: Client) {
        npc.CalculateMaxHit(true)
        var hitDiff = Randoms.randomMinusOne(MELEE_MAX_DMG + 1)
        if (!npc.landHit(target, true)) hitDiff = 0
        npc.performAnimation(ANIM_KBD_MELEE, 0)
        target.dealDamage(hitDiff, Entity.hitType.STANDARD, npc, Entity.damageType.MELEE)
        npc.lastAttack = MELEE_SPEED
    }

    private fun attackDragonfire(npc: Npc, target: Client) {
        val damage = Randoms.randomMinusOne(FIRE_MAX_DMG + 1)
        npc.sendArrow(target, -1, PROJ_DRAGONFIRE)
        val delay = npc.getDistanceDelay(npc.calculateDistanceTo(target), true)
        handleBreathHit(npc, target, GFX_DRAGONFIRE_HIT, delay, damage, true) {}
        npc.lastAttack = BREATH_SPEED
    }

    private fun attackShock(npc: Npc, target: Client) {
        val damage = Randoms.randomMinusOne(SPECIAL_MAX_DMG + 1)
        npc.setText("Tss rawr!!")
        npc.sendArrow(target, -1, PROJ_SHOCK)
        val delay = npc.getDistanceDelay(npc.calculateDistanceTo(target), false)
        handleBreathHit(npc, target, GFX_SHOCK_HIT, delay, damage, false) {
            drainCombatStats(target)
        }
        npc.lastAttack = BREATH_SPEED
    }

    private fun attackIce(npc: Npc, target: Client) {
        val damage = Randoms.randomMinusOne(SPECIAL_MAX_DMG + 1)
        npc.setText("Tsss!")
        npc.sendArrow(target, -1, PROJ_ICE)
        val delay = npc.getDistanceDelay(npc.calculateDistanceTo(target), true)
        handleBreathHit(npc, target, GFX_ICE_HIT, delay, damage, false) {
            freezePlayer(target)
        }
        npc.lastAttack = BREATH_SPEED
    }

    private fun attackPoison(npc: Npc, target: Client) {
        val damage = Randoms.randomMinusOne(SPECIAL_MAX_DMG + 1)
        npc.setText("Rawr!!")
        npc.sendArrow(target, -1, PROJ_POISON)
        val delay = npc.getDistanceDelay(npc.calculateDistanceTo(target), false)
        handleBreathHit(npc, target, GFX_POISON_HIT, delay, damage, false) {
            applyPoison(target)
        }
        npc.lastAttack = BREATH_SPEED
    }

    private fun handleBreathHit(
        npc: Npc,
        target: Client,
        hitGfx: Int,
        delayTicks: Int,
        damage: Int,
        crit: Boolean,
        onHit: () -> Unit,
    ) {
        GameEventScheduler.runLaterMs(delayTicks * 600) {
            if (target.disconnected || target.isDeathSequenceActive || target.currentHealth < 1) return@runLaterMs
            target.callGfxMask(hitGfx, 100)
            target.dealDamage(
                damage,
                if (crit) Entity.hitType.CRIT else Entity.hitType.STANDARD,
                npc,
                Entity.damageType.FIRE_BREATH,
            )
            onHit()
        }
    }

    private fun freezePlayer(target: Client) {
        target.snareTimer = FREEZE_TICKS
        target.resetWalkingQueue()
        target.send(SendMessage("You have been frozen!"))
    }

    private fun drainCombatStats(target: Client) {
        target.send(SendMessage("You are shocked and weakened!"))
        val drained = arrayOf(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC)
        for (skill in drained) {
            val idx = skill.id
            target.boostedLevel[idx] = max(target.boostedLevel[idx] - 2, 0)
            ProgressionService.refresh(target, skill)
        }
    }

    private fun applyPoison(target: Client) {
        target.poisonDamage = 8
        target.send(SendMessage("You have been poisoned!"))
    }

    private fun Npc.calculateDistanceTo(target: Client): Int {
        val dx = position.x - target.position.x
        val dy = position.y - target.position.y
        val dist = sqrt((dx * dx + dy * dy).toDouble())
        return floor(dist).toInt()
    }
}
