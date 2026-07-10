package net.dodian.uber.game.skill.firemaking

import net.dodian.uber.game.engine.event.GameEventScheduler
import net.dodian.uber.game.engine.systems.world.item.Ground
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.GroundItem
import net.dodian.uber.game.model.objects.GlobalObject
import net.dodian.uber.game.model.objects.WorldObject
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.engine.systems.action.PolicyPreset

object Firemaking {
    private const val TINDERBOX = 590
    private const val FIRE_OBJECT_ID = 5249
    private const val ANIM_LIGHT_FIRE = 733
    private const val ASHES_ITEM_ID = 592

    @JvmStatic
    fun handleItemCombination(client: Client, itemUsed: Int, useWith: Int): Boolean {
        if (itemUsed != TINDERBOX && useWith != TINDERBOX) {
            return false
        }
        val log = FiremakingData.findLog(itemUsed, useWith) ?: return false
        if (client.getLevel(Skill.FIREMAKING) < log.level) {
            client.sendMessage("You need a firemaking level of ${log.level} to burn ${log.name}.")
            return true
        }
        client.deleteItem(log.itemId, 1)
        client.checkItemUpdate()
        client.performAnimation(ANIM_LIGHT_FIRE, 0)
        client.sendMessage("You light the ${log.name}.")

        val pos = client.position
        val playerSlot = client.slot

        val fire = WorldObject(FIRE_OBJECT_ID, pos.x, pos.y, pos.z, 10, 0, 0)
        GlobalObject.addGlobalObject(fire, log.durationTicks * 600)

        GameEventScheduler.runLaterMs(log.durationTicks * 600) {
            if (client.disconnected) return@runLaterMs
            Ground.addItem(GroundItem(pos, ASHES_ITEM_ID, 1, playerSlot, -1))
            client.sendMessage("The fire has burnt out.")
        }

        client.resetAction()
        return true
    }

    @JvmStatic
    fun handleLogOnFire(client: Client, itemId: Int): Boolean {
        val log = FiremakingData.findLog(itemId, itemId) ?: return false
        if (client.getLevel(Skill.FIREMAKING) < log.level) {
            client.sendMessage("You need a firemaking level of ${log.level} to burn ${log.name}.")
            return true
        }
        client.deleteItem(log.itemId, 1)
        client.checkItemUpdate()
        ProgressionService.addXp(client, log.xp, Skill.FIREMAKING)
        client.sendMessage("You add the ${log.name} to the fire.")
        return true
    }
}

object FiremakingSkillPlugin : SkillPlugin {
    private const val TINDERBOX = 590
    private const val FIRE_OBJECT_ID = 5249
    private val logIds = intArrayOf(1511, 1521, 1519, 1517, 1515, 1513)

    override val definition =
        skillPlugin(name = "Firemaking", skill = Skill.FIREMAKING) {
            for (logId in logIds) {
                itemOnItem(preset = PolicyPreset.PRODUCTION, leftItemId = TINDERBOX, rightItemId = logId) { client, itemUsed, otherItem ->
                    Firemaking.handleItemCombination(client, itemUsed, otherItem)
                }
            }
            itemOnObject(preset = PolicyPreset.PRODUCTION, objectIds = intArrayOf(FIRE_OBJECT_ID), itemIds = logIds) { client, objectId, position, obj, itemId, itemSlot, interfaceId ->
                Firemaking.handleLogOnFire(client, itemId)
            }
        }
}
