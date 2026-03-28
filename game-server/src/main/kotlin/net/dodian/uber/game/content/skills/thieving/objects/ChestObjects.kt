package net.dodian.uber.game.content.skills.thieving.objects

import net.dodian.uber.game.world.cache.`object`.GameObjectData
import net.dodian.uber.game.content.objects.ObjectContent
import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.model.`object`.GlobalObject
import net.dodian.uber.game.model.`object`.Object as GameObject
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.content.skills.core.progression.SkillProgressionService
import net.dodian.uber.game.content.skills.core.runtime.SkillingRandomEventService
import net.dodian.uber.game.content.skills.thieving.ThievingPlugin
import net.dodian.uber.game.systems.api.content.ContentInteraction
import net.dodian.uber.game.systems.api.content.ContentObjectInteractionPolicy
import net.dodian.uber.game.persistence.audit.ItemLog
import net.dodian.utilities.Utils

object ChestObjects : ObjectContent {
    override val objectIds: IntArray = ThievingObjectComponents.chestObjects

    override fun clickInteractionPolicy(
        option: Int,
        objectId: Int,
        position: Position,
        obj: GameObjectData?,
    ): ContentObjectInteractionPolicy? {
        if (option != 1 && option != 2) {
            return null
        }
        return ContentInteraction.nearestBoundaryCardinalPolicy()
    }

    override fun onFirstClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        if (objectId == 20873 || objectId == 6847) {
            ThievingPlugin.attempt(client, objectId, position)
            return true
        }
        val specialChest =
            SkillDataRegistry.thievingSpecialChests().firstOrNull {
                it.objectId == objectId &&
                    it.location.x == position.x &&
                    it.location.y == position.y &&
                    it.location.z == position.z
            }
        if (specialChest != null) {
            if (client.chestEventOccur) {
                return true
            }
            if (specialChest.premiumOnly && !client.premium) {
                client.resetPos()
                return true
            }
            if (client.getLevel(Skill.THIEVING) < specialChest.requiredLevel) {
                client.sendMessage("You must be level ${specialChest.requiredLevel} thieving to open this chest")
                return true
            }
            if (client.freeSlots() < 1) {
                client.sendMessage("You need atleast one free inventory slot!")
                return true
            }
            val lockKey =
                when (specialChest.chestKey.lowercase()) {
                    "yanille" -> ContentInteraction.YANILLE_CHEST
                    "legends" -> ContentInteraction.LEGENDS_CHEST
                    else -> ContentInteraction.YANILLE_CHEST
                }
            if (!ContentInteraction.tryAcquireMs(client, lockKey, specialChest.lockMs)) {
                return true
            }
            val emptyObj =
                GameObject(
                    specialChest.replacementObjectId,
                    position.x,
                    position.y,
                    position.z,
                    specialChest.replacementType,
                    specialChest.replacementFace,
                    objectId,
                )
            if (!GlobalObject.addGlobalObject(emptyObj, specialChest.replacementLifetimeMs)) {
                return true
            }
            val roll = Math.random() * 100
            if (roll <= specialChest.rareRewardChancePercent) {
                val rewards = specialChest.rareRewards
                if (rewards.isNotEmpty()) {
                    val reward = rewards[(Math.random() * rewards.size).toInt()]
                    val itemId = reward.itemId
                    client.sendMessage("You have recieved a ${client.getItemName(itemId)}!")
                    client.addItem(itemId, 1)
                    ItemLog.playerGathering(client, itemId, 1, client.position.copy(), "Thieving")
                    client.yell("[Server] - ${client.playerName} has just received from the ${specialChest.chestKey.replaceFirstChar { it.uppercase() }} chest a ${client.getItemName(itemId)}")
                }
            } else {
                val coins = specialChest.coinsMin + Utils.random(specialChest.coinsMaxOffset)
                client.sendMessage("You find $coins coins inside the chest")
                client.addItem(995, coins)
                ItemLog.playerGathering(client, 995, coins, client.position.copy(), "Thieving")
            }
            if (client.equipment[Equipment.Slot.HEAD.id] == 2631) {
                SkillProgressionService.gainXp(client, specialChest.baseXp, Skill.THIEVING)
            }
            client.checkItemUpdate()
            client.chestEvent++
            client.stillgfx(444, position.y, position.x)
            SkillingRandomEventService.trigger(client, specialChest.randomEventXp)
            return true
        }
        return false
    }

    override fun onSecondClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        return when (objectId) {
            378 -> {
                client.sendMessage("This chest is empty!")
                true
            }
            20873, 11729, 11730, 11731, 11732, 11733, 11734 -> {
                ThievingPlugin.attempt(client, objectId, position)
                true
            }
            else -> false
        }
    }
}
