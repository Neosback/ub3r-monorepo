package net.dodian.uber.game.npc

import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.loop.GameThreadTimers
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.objects.travel.LegendsGuildGateService

private const val GUARD_ID = 3951
private const val GUARD_SPAWN_Y = 3349
private const val GUARD_SPAWN_Z = 0
private const val GUARD_WEST_X = 2727
private const val GUARD_EAST_X = 2730

internal object LegendsGuard : NpcFamily by npcFamily("Legends' Guard", GUARD_ID, block = {
    definition {
        examine = "A Legends' Guild guard; he protects the entrance to the Legends' Guild."
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleLegendsGuardTalkTo)
    }

    spawns {
        spawn(GUARD_WEST_X, GUARD_SPAWN_Y)
        spawn(GUARD_EAST_X, GUARD_SPAWN_Y)
    }
}) {

    @JvmStatic
    fun triggerGateChat(client: Client) {
        val manager = Server.npcManager ?: return

        val westGuard = manager.findNpcByIdAtPosition(GUARD_ID, GUARD_WEST_X, GUARD_SPAWN_Y, GUARD_SPAWN_Z)
        westGuard?.setText("Enjoy your stay, ${client.playerName}")

        val topSkillName = pickRandomTopSkill(client)
        if (topSkillName != null) {
            GameThreadTimers.schedule("legend-guard-rank", 1200L) {
                if (client.isActive && !client.disconnected) {
                    val eastGuard = Server.npcManager
                        ?.findNpcByIdAtPosition(GUARD_ID, GUARD_EAST_X, GUARD_SPAWN_Y, GUARD_SPAWN_Z)
                    eastGuard?.setText("Did you know you are number one in $topSkillName?")
                }
            }
        }
    }

    private fun pickRandomTopSkill(client: Client): String? {
        val candidates = ArrayList<String>()
        for (skill in Skill.enabledSkills()) {
            val playerXp = client.getExperience(skill)
            if (playerXp <= 0) continue
            val topXp = LegendsGuardRankingCache.topXp(skill)
            if (playerXp >= topXp) {
                candidates.add(
                    skill.getName().replaceFirstChar { c -> c.uppercaseChar() }
                )
            }
        }
        return if (candidates.isNotEmpty()) candidates.random() else null
    }
}

@Suppress("UNUSED_PARAMETER")
private fun handleLegendsGuardTalkTo(client: Client, npc: Npc): Boolean {
    LegendsGuildGateService.openForGuardTalk(client, npc)
    return true
}
