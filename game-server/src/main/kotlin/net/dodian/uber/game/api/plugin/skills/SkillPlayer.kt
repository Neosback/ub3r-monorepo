package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.engine.systems.inventory.inventoryTransaction
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces

/**
 * The player surface available to skill content.
 *
 * Network/protocol details deliberately stay behind this boundary.  The
 * concrete adapter is created by the skill dispatcher for one interaction.
 */
interface SkillPlayer {
    val skills: SkillLevels
    val inventory: SkillInventory
    val actions: SkillActions
    val ui: SkillUi
    val world: SkillWorld
}

interface SkillLevels {
    fun current(skill: Skill): Int
    fun base(skill: Skill): Int
    fun experience(skill: Skill): Int
    fun gainXp(amount: Int, skill: Skill): Boolean
}

interface SkillInventory {
    fun contains(itemId: Int, amount: Int = 1): Boolean
    fun amount(itemId: Int): Int
    fun freeSlots(): Int
    fun add(itemId: Int, amount: Int = 1): Boolean
    fun remove(itemId: Int, amount: Int = 1): Boolean
    fun transaction(block: SkillInventoryTransaction.() -> Unit): Boolean
    fun refresh()
}

interface SkillInventoryTransaction {
    fun require(itemId: Int, amount: Int = 1): Boolean
    fun remove(itemId: Int, amount: Int = 1): Boolean
    fun removeAt(slot: Int, itemId: Int, amount: Int = 1): Boolean
    fun add(itemId: Int, amount: Int = 1): Boolean
}

interface SkillActions {
    fun animate(id: Int, delay: Int = 0)
    fun stop()
    fun beginSession(key: String): Boolean
    fun endSession(key: String)
}

interface SkillUi {
    fun message(text: String)
    fun string(text: String, componentId: Int)
    fun open(interfaceId: Int)
    fun close()
}

interface SkillWorld {
    val position: Position
    fun distanceTo(x: Int, y: Int): Int
}

internal class ClientSkillPlayer(private val client: Client) : SkillPlayer {
    override val skills: SkillLevels = object : SkillLevels {
        override fun current(skill: Skill): Int = client.getLevel(skill)
        override fun base(skill: Skill): Int = client.getSkillLevel(skill)
        override fun experience(skill: Skill): Int = client.getExperience(skill)
        override fun gainXp(amount: Int, skill: Skill): Boolean = ProgressionService.addXp(client, amount, skill)
    }

    override val inventory: SkillInventory = object : SkillInventory {
        override fun contains(itemId: Int, amount: Int): Boolean = client.getInvAmt(itemId) >= amount
        override fun amount(itemId: Int): Int = client.getInvAmt(itemId)
        override fun freeSlots(): Int = client.freeSlots()
        override fun add(itemId: Int, amount: Int): Boolean = client.addItem(itemId, amount)
        override fun remove(itemId: Int, amount: Int): Boolean {
            if (client.getInvAmt(itemId) < amount) return false
            client.deleteItem(itemId, amount)
            return true
        }
        override fun transaction(block: SkillInventoryTransaction.() -> Unit): Boolean =
            client.inventoryTransaction {
                val staged = this
                block(object : SkillInventoryTransaction {
                    override fun require(itemId: Int, amount: Int) = staged.require(itemId, amount)
                    override fun remove(itemId: Int, amount: Int) = staged.remove(itemId, amount)
                    override fun removeAt(slot: Int, itemId: Int, amount: Int) = staged.removeAt(slot, itemId, amount)
                    override fun add(itemId: Int, amount: Int) = staged.add(itemId, amount)
                })
            }
        override fun refresh() = client.checkItemUpdate()
    }

    override val actions: SkillActions = object : SkillActions {
        override fun animate(id: Int, delay: Int) = client.performAnimation(id, delay)
        override fun stop() = client.resetAction()
        override fun beginSession(key: String): Boolean =
            net.dodian.uber.game.skill.runtime.action.SkillStateCoordinator.beginSession(client, key)
        override fun endSession(key: String) =
            net.dodian.uber.game.skill.runtime.action.SkillStateCoordinator.endSession(client, key)
    }

    override val ui: SkillUi = object : SkillUi {
        override fun message(text: String) = client.sendMessage(text)
        override fun string(text: String, componentId: Int) = client.sendString(text, componentId)
        override fun open(interfaceId: Int) = client.openInterface(interfaceId)
        override fun close() = client.send(RemoveInterfaces())
    }

    override val world: SkillWorld = object : SkillWorld {
        override val position: Position get() = client.position
        override fun distanceTo(x: Int, y: Int): Int = client.distanceToPoint(x, y)
    }

    internal fun protocolClient(): Client = client
}

internal fun Client.asSkillPlayer(): SkillPlayer = ClientSkillPlayer(this)

/** Internal migration bridge; never expose this from a skill-plugin handler API. */
internal fun SkillPlayer.protocolClient(): Client = (this as ClientSkillPlayer).protocolClient()
