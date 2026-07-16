package net.dodian.uber.skills.testkit

import net.dodian.uber.game.api.content.ContentEconomy
import net.dodian.uber.game.api.content.ContentEquipment
import net.dodian.uber.game.api.content.ContentFeatures
import net.dodian.uber.game.api.content.ContentSocial
import net.dodian.uber.game.api.plugin.skills.SkillActions
import net.dodian.uber.game.api.plugin.skills.SkillInventory
import net.dodian.uber.game.api.plugin.skills.SkillInventoryTransaction
import net.dodian.uber.game.api.plugin.skills.SkillLevels
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import net.dodian.uber.game.api.plugin.skills.SkillUi
import net.dodian.uber.game.api.plugin.skills.SkillWorld
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.player.skills.Skill

/** Protocol-free deterministic player double for modular skill tests. */
class FakeSkillPlayer(initialItems: Map<Int, Int> = emptyMap()) : SkillPlayer {
    val messages = mutableListOf<String>()
    val animations = mutableListOf<Pair<Int, Int>>()
    val strings = mutableMapOf<Int, String>()
    var refreshCount = 0
        private set
    var positionValue = Position(3200, 3200, 0)
    private val itemAmounts = initialItems.filterValues { it > 0 }.toMutableMap()
    private val xp = mutableMapOf<Skill, Int>()
    private val levels = mutableMapOf<Skill, Int>()
    private val equipmentItems = mutableMapOf<Int, Pair<Int, Int>>()
    private var sessionKey: String? = null

    override val skills = object : SkillLevels {
        override fun current(skill: Skill) = levels[skill] ?: 1
        override fun base(skill: Skill) = levels[skill] ?: 1
        override fun experience(skill: Skill) = xp[skill] ?: 0
        override fun gainXp(amount: Int, skill: Skill): Boolean {
            if (amount <= 0) return false
            xp[skill] = experience(skill) + amount
            return true
        }
    }
    override val inventory = object : SkillInventory {
        override fun contains(itemId: Int, amount: Int) = this@FakeSkillPlayer.amount(itemId) >= amount
        override fun amount(itemId: Int) = this@FakeSkillPlayer.amount(itemId)
        override fun freeSlots() = (28 - itemAmounts.size).coerceAtLeast(0)
        override fun add(itemId: Int, amount: Int): Boolean {
            if (itemId < 0 || amount <= 0) return false
            itemAmounts[itemId] = (itemAmounts[itemId] ?: 0) + amount
            return true
        }
        override fun remove(itemId: Int, amount: Int): Boolean {
            if (!contains(itemId, amount)) return false
            setAmount(itemId, this@FakeSkillPlayer.amount(itemId) - amount)
            return true
        }
        override fun transaction(block: SkillInventoryTransaction.() -> Unit): Boolean {
            val staged = itemAmounts.toMutableMap()
            var failed = false
            val transaction = object : SkillInventoryTransaction {
                override fun require(itemId: Int, amount: Int): Boolean {
                    val present = itemId >= 0 && amount > 0 && staged[itemId].orZero() >= amount
                    if (!present) failed = true
                    return present
                }
                override fun remove(itemId: Int, amount: Int): Boolean {
                    if (itemId < 0 || amount <= 0 || staged[itemId].orZero() < amount) return false.also { failed = true }
                    staged[itemId] = staged[itemId].orZero() - amount
                    if (staged[itemId] == 0) staged.remove(itemId)
                    return true
                }
                override fun removeAt(slot: Int, itemId: Int, amount: Int) = remove(itemId, amount)
                override fun add(itemId: Int, amount: Int): Boolean {
                    if (itemId < 0 || amount <= 0) return false.also { failed = true }
                    staged[itemId] = staged[itemId].orZero() + amount
                    return true
                }
            }
            transaction.block()
            if (failed) return false
            itemAmounts.clear(); itemAmounts.putAll(staged); refresh()
            return true
        }
        override fun refresh() { refreshCount++ }
    }
    override val actions = object : SkillActions {
        override fun animate(id: Int, delay: Int) { animations += id to delay }
        override fun stop() { sessionKey = null }
        override fun lockMovement(locked: Boolean) = Unit
        override fun beginSession(key: String): Boolean = if (sessionKey == null || sessionKey == key) { sessionKey = key; true } else false
        override fun endSession(key: String) { if (sessionKey == key) sessionKey = null }
        override fun activeSessionKey(): String? = sessionKey
    }
    override val ui = object : SkillUi {
        override fun message(text: String) { messages += text }
        override fun string(text: String, componentId: Int) { strings[componentId] = text }
        override fun open(interfaceId: Int) = Unit
        override fun close() = Unit
        override fun npcDialogue(dialogueId: Int, npcId: Int) = Unit
    }
    override val world = object : SkillWorld {
        override val position: Position get() = positionValue
        override fun distanceTo(x: Int, y: Int) = maxOf(kotlin.math.abs(position.x - x), kotlin.math.abs(position.y - y))
        override fun teleport(destination: Position) { positionValue = destination }
    }
    override val equipment = object : ContentEquipment {
        override fun item(slot: Int) = equipmentItems[slot]?.first ?: -1
        override fun amount(slot: Int) = equipmentItems[slot]?.second ?: 0
        override fun refresh() = Unit
    }
    override val economy = object : ContentEconomy {
        override fun bankAmount(itemId: Int) = 0
        override fun openBank() = Unit
        override fun openShop(shopId: Int) = Unit
    }
    override val social = ContentSocial { false }
    override val features = object : ContentFeatures {
        override val bankingEnabled = true
        override val shoppingEnabled = true
        override val tradingEnabled = true
        override val duelingEnabled = true
    }

    fun amount(itemId: Int): Int = itemAmounts[itemId].orZero()
    fun setLevel(skill: Skill, level: Int) { levels[skill] = level.coerceAtLeast(1) }
    private fun setAmount(itemId: Int, amount: Int) { if (amount <= 0) itemAmounts.remove(itemId) else itemAmounts[itemId] = amount }
    private fun Int?.orZero(): Int = this ?: 0
}
