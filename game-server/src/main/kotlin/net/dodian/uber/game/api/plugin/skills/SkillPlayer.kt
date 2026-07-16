package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.engine.systems.inventory.inventoryTransaction
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces
import net.dodian.uber.game.api.content.ContentActionControl
import net.dodian.uber.game.api.content.ContentEconomy
import net.dodian.uber.game.api.content.ContentEquipment
import net.dodian.uber.game.api.content.ContentFeatures
import net.dodian.uber.game.api.content.ContentInventory
import net.dodian.uber.game.api.content.ContentPlayer
import net.dodian.uber.game.api.content.ContentSocial
import net.dodian.uber.game.api.content.ContentUi
import net.dodian.uber.game.api.content.ContentWorld
import net.dodian.uber.game.engine.config.FeatureStateService

/**
 * The player surface available to skill content.
 *
 * Network/protocol details deliberately stay behind this boundary.  The
 * concrete adapter is created by the skill dispatcher for one interaction.
 */
interface SkillPlayer : ContentPlayer {
    val skills: SkillLevels
    override val inventory: SkillInventory
    override val actions: SkillActions
    override val ui: SkillUi
    override val world: SkillWorld
}

interface SkillLevels {
    fun current(skill: Skill): Int
    fun base(skill: Skill): Int
    fun experience(skill: Skill): Int
    fun gainXp(amount: Int, skill: Skill): Boolean
}

interface SkillInventory : ContentInventory {
    fun transaction(block: SkillInventoryTransaction.() -> Unit): Boolean
    override fun refresh()
}

interface SkillInventoryTransaction {
    fun require(itemId: Int, amount: Int = 1): Boolean
    fun remove(itemId: Int, amount: Int = 1): Boolean
    fun removeAt(slot: Int, itemId: Int, amount: Int = 1): Boolean
    fun add(itemId: Int, amount: Int = 1): Boolean
}

interface SkillActions : ContentActionControl

interface SkillUi : ContentUi

interface SkillWorld : ContentWorld

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
        override fun lockMovement(locked: Boolean) = client.setMovementLocked(locked)
        override fun beginSession(key: String): Boolean =
            net.dodian.uber.game.skill.runtime.action.SkillStateCoordinator.beginSession(client, key)
        override fun endSession(key: String) =
            net.dodian.uber.game.skill.runtime.action.SkillStateCoordinator.endSession(client, key)
        override fun activeSessionKey(): String? = client.activeSkillSessionKey
    }

    override val ui: SkillUi = object : SkillUi {
        override fun message(text: String) = client.sendMessage(text)
        override fun string(text: String, componentId: Int) = client.sendString(text, componentId)
        override fun open(interfaceId: Int) = client.openInterface(interfaceId)
        override fun close() = client.send(RemoveInterfaces())
        override fun npcDialogue(dialogueId: Int, npcId: Int) = client.startNpcDialogue(dialogueId, npcId)
    }

    override val world: SkillWorld = object : SkillWorld {
        override val position: Position get() = client.position
        override fun distanceTo(x: Int, y: Int): Int = client.distanceToPoint(x, y)
        override fun teleport(destination: Position) = client.transport(destination)
    }

    override val equipment: ContentEquipment = object : ContentEquipment {
        override fun item(slot: Int): Int = client.equipment.getOrElse(slot) { -1 }
        override fun amount(slot: Int): Int = client.equipmentN.getOrElse(slot) { 0 }
        override fun refresh() = client.equipment.indices.forEach { slot ->
            client.setEquipment(client.equipment[slot], client.equipmentN[slot], slot)
        }
    }

    override val economy: ContentEconomy = object : ContentEconomy {
        override fun bankAmount(itemId: Int): Int = client.getBankAmt(itemId)
        override fun openBank() = client.openUpBankRouted()
        override fun openShop(shopId: Int) = client.openUpShopRouted(shopId)
    }

    override val social: ContentSocial = ContentSocial { encodedName -> client.hasFriend(encodedName) }

    override val features: ContentFeatures = object : ContentFeatures {
        override val bankingEnabled get() = FeatureStateService.banking.get()
        override val shoppingEnabled get() = FeatureStateService.shopping.get()
        override val tradingEnabled get() = FeatureStateService.trading.get()
        override val duelingEnabled get() = FeatureStateService.dueling.get()
    }

    internal fun protocolClient(): Client = client
}

internal fun Client.asSkillPlayer(): SkillPlayer = ClientSkillPlayer(this)

/** Internal migration bridge; never expose this from a skill-plugin handler API. */
internal fun SkillPlayer.protocolClient(): Client = (this as ClientSkillPlayer).protocolClient()
