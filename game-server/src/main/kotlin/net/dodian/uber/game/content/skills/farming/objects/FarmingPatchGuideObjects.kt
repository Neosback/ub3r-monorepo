package net.dodian.uber.game.content.skills.farming.objects

import net.dodian.uber.game.world.cache.`object`.GameObjectData
import net.dodian.uber.game.content.objects.ObjectContent
import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.netty.listener.out.SendMessage
import net.dodian.uber.game.content.skills.guide.SkillGuidePlugin

object FarmingPatchGuideObjects : ObjectContent {
    override val objectIds: IntArray = SkillDataRegistry.farmingPatchGuideObjects().distinct().sorted().toIntArray()

    override fun onSecondClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        if (objectId == 7962) {
            client.sendMessage("You inspect the monolith, but can't make sense of the inscription.")
            return true
        }
        return false
    }

    override fun onFourthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        return when {
            (objectId in 8550..8557) || objectId == 27114 || objectId == 27113 -> {
                SkillGuidePlugin.open(client, Skill.FARMING.id, 0)
                true
            }
            (objectId in 7847..7850) || objectId == 27111 -> {
                SkillGuidePlugin.open(client, Skill.FARMING.id, 1)
                true
            }
            objectId in 7577..7580 -> {
                SkillGuidePlugin.open(client, Skill.FARMING.id, 2)
                true
            }
            (objectId in 8150..8153) || objectId == 27115 -> {
                SkillGuidePlugin.open(client, Skill.FARMING.id, 3)
                true
            }
            (objectId in 8389..8391) || objectId == 19147 -> {
                SkillGuidePlugin.open(client, Skill.FARMING.id, 4)
                true
            }
            (objectId in 7962..7965) || objectId == 26579 -> {
                SkillGuidePlugin.open(client, Skill.FARMING.id, 5)
                true
            }
            else -> false
        }
    }

    override fun onFifthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
}
