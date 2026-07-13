package net.dodian.uber.game.api.interaction

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.Server

@GameContentDsl
class InteractionRequirementBuilder(val player: Client) {
    var passed = true

    fun level(skill: Skill, requiredLevel: Int, message: String? = null) {
        if (!passed) return
        if (player.getLevel(skill) < requiredLevel) {
            passed = false
            player.sendMessage(message ?: "You need a ${skill.getName()} level of $requiredLevel to do this.")
        }
    }

    fun hasItem(itemId: Int, amount: Int = 1, message: String? = null) {
        if (!passed) return
        if (!player.playerHasItem(itemId, amount)) {
            passed = false
            val itemName = Server.itemManager.getName(itemId)
            player.sendMessage(message ?: "You need $amount x $itemName to do this.")
        }
    }

    fun freeSlots(requiredSlots: Int = 1, message: String? = null) {
        if (!passed) return
        if (player.freeSlots() < requiredSlots) {
            passed = false
            player.sendMessage(message ?: "You need at least $requiredSlots free inventory slots.")
        }
    }
}

inline fun InteractionContext.requires(block: InteractionRequirementBuilder.() -> Unit): Boolean {
    val builder = InteractionRequirementBuilder(player)
    builder.block()
    return builder.passed
}

inline fun Client.requires(block: InteractionRequirementBuilder.() -> Unit): Boolean {
    val builder = InteractionRequirementBuilder(this)
    builder.block()
    return builder.passed
}
