package net.dodian.uber.game.npc

import java.util.Objects
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.model.player.skills.Skills

internal object Wydin : NpcFamily by npcFamily("Zombie monk", 557, block = {
    cache {
        name = "Zombie monk"
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleWydinBlessPlayer)
    }

    spawns {
        spawn(3256, 2780)
    }
})

private fun handleWydinBlessPlayer(client: Client, npc: Npc): Boolean {
    npc.performAnimation(5643, 0)
    for (skill in listOf(0, 1, 2, 4)) {
        val skillType = Objects.requireNonNull(Skill.getSkill(skill))
        val maxLevel = Skills.getLevelForExperience(client.getExperience(skillType))
        client.boost(5 + (maxLevel * 0.15).toInt(), skillType)
    }
    val ticks = (1 + Skills.getLevelForExperience(client.getExperience(Skill.HERBLORE))) * 2
    client.addEffectTime(2, 200 + ticks)
    client.sendMessage("The monk boost your stats!")
    return true
}
