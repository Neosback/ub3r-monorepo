package net.dodian.uber.game.api.content

import net.dodian.uber.game.model.Position

/**
 * Protocol-free player surface for gameplay/content modules. Implementations
 * are provided by the engine; content must never down-cast it to a network
 * session or access entity arrays directly.
 */
interface ContentPlayer {
    val inventory: ContentInventory
    val equipment: ContentEquipment
    val economy: ContentEconomy
    val actions: ContentActionControl
    val ui: ContentUi
    val world: ContentWorld
    val social: ContentSocial
    val features: ContentFeatures
}

interface ContentInventory {
    fun contains(itemId: Int, amount: Int = 1): Boolean
    fun amount(itemId: Int): Int
    fun freeSlots(): Int
    fun add(itemId: Int, amount: Int = 1): Boolean
    fun remove(itemId: Int, amount: Int = 1): Boolean
    fun refresh()
}

interface ContentEquipment {
    fun item(slot: Int): Int
    fun amount(slot: Int): Int
    fun refresh()
}

interface ContentEconomy {
    fun bankAmount(itemId: Int): Int
    fun openBank()
    fun openShop(shopId: Int)
}

interface ContentActionControl {
    fun animate(id: Int, delay: Int = 0)
    fun stop()
    fun lockMovement(locked: Boolean)
    fun beginSession(key: String): Boolean
    fun endSession(key: String)
    fun activeSessionKey(): String?
}

interface ContentUi {
    fun message(text: String)
    fun string(text: String, componentId: Int)
    fun open(interfaceId: Int)
    fun close()
    fun npcDialogue(dialogueId: Int, npcId: Int)
}

interface ContentWorld {
    val position: Position
    fun distanceTo(x: Int, y: Int): Int
    fun teleport(destination: Position)
}

fun interface ContentSocial {
    fun hasFriend(encodedName: Long): Boolean
}

interface ContentFeatures {
    val bankingEnabled: Boolean
    val shoppingEnabled: Boolean
    val tradingEnabled: Boolean
    val duelingEnabled: Boolean
}
