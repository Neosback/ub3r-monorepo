package net.dodian.uber.game.engine.systems.inventory

import net.dodian.uber.game.Server
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.GameItem
import net.dodian.uber.game.persistence.player.PlayerSaveSegment

/**
 * Stages changes to one or more player-owned item containers.  A container is
 * never written until every requested operation has been validated.  This is
 * deliberately game-thread owned; callers must not retain a transaction over
 * a tick or expose it to asynchronous work.
 */
class EconomyTransaction private constructor() {
    private val arrays = linkedMapOf<ArrayKey, ArrayContainer>()
    private val offers = linkedMapOf<Client, OfferContainer>()
    private var failed = false

    fun inventory(client: Client): Items = array(client, Container.INVENTORY)
    fun bank(client: Client): Items = array(client, Container.BANK)
    fun equipment(client: Client): Items = array(client, Container.EQUIPMENT)
    fun offer(client: Client): OfferItems = offers.getOrPut(client) { OfferContainer(client) }

    fun failed(): Boolean = failed

    fun commit(): Boolean {
        if (failed || arrays.values.any { it.failed } || offers.values.any { it.failed }) return false
        arrays.values.forEach { it.commit() }
        offers.values.forEach { it.commit() }
        return true
    }

    private fun array(client: Client, kind: Container): Items {
        val key = ArrayKey(client, kind)
        return arrays.getOrPut(key) { ArrayContainer(client, kind) }
    }

    private data class ArrayKey(val client: Client, val kind: Container)
    private enum class Container { INVENTORY, BANK, EQUIPMENT }

    /** Item ids supplied to this API are always unencoded game ids. */
    interface Items {
        fun amountOf(itemId: Int): Int
        fun require(itemId: Int, amount: Int = 1): Boolean
        fun remove(itemId: Int, amount: Int = 1): Boolean
        fun removeAt(slot: Int, itemId: Int, amount: Int = 1): Boolean
        fun add(itemId: Int, amount: Int = 1): Boolean
        fun replaceAt(slot: Int, removeItemId: Int, addItemId: Int, amount: Int = 1): Boolean
    }

    interface OfferItems {
        fun amountOf(itemId: Int): Int
        fun require(itemId: Int, amount: Int = 1): Boolean
        fun remove(itemId: Int, amount: Int = 1): Boolean
        fun removeAt(slot: Int, itemId: Int, amount: Int = 1): Boolean
        fun add(itemId: Int, amount: Int = 1): Boolean
        fun clear()
        fun snapshot(): List<GameItem>
    }

    private class ArrayContainer(
        private val client: Client,
        private val kind: Container,
    ) : Items {
        private val ids: IntArray
        private val amounts: IntArray
        private var changed = false
        var failed = false
            private set

        init {
            val sourceIds = when (kind) {
                Container.INVENTORY -> client.playerItems
                Container.BANK -> client.bankItems
                Container.EQUIPMENT -> client.equipment
            }
            val sourceAmounts = when (kind) {
                Container.INVENTORY -> client.playerItemsN
                Container.BANK -> client.bankItemsN
                Container.EQUIPMENT -> client.equipmentN
            }
            ids = sourceIds.copyOf()
            amounts = sourceAmounts.copyOf()
        }

        override fun amountOf(itemId: Int): Int = ids.indices.sumOf { slot ->
            if (decode(ids[slot]) == itemId) amounts[slot].toLong() else 0L
        }.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

        override fun require(itemId: Int, amount: Int): Boolean =
            valid(itemId, amount) && amountOf(itemId) >= amount || fail()

        override fun remove(itemId: Int, amount: Int): Boolean {
            if (!require(itemId, amount)) return false
            var remaining = amount
            for (slot in ids.indices) {
                if (decode(ids[slot]) != itemId || remaining == 0) continue
                val removed = minOf(amounts[slot], remaining)
                amounts[slot] -= removed
                remaining -= removed
                if (amounts[slot] == 0) ids[slot] = 0
            }
            changed = true
            return true
        }

        override fun removeAt(slot: Int, itemId: Int, amount: Int): Boolean {
            if (!valid(itemId, amount) || slot !in ids.indices || decode(ids[slot]) != itemId || amounts[slot] < amount) return fail()
            amounts[slot] -= amount
            if (amounts[slot] == 0) ids[slot] = 0
            changed = true
            return true
        }

        override fun add(itemId: Int, amount: Int): Boolean {
            if (!valid(itemId, amount) || failed) return fail()
            // Banks represent every item type as a single quantity-bearing
            // entry, while inventory/equipment preserve non-stackable slots.
            val stackable = kind == Container.BANK || Server.itemManager.isStackable(itemId)
            if (stackable) {
                val existing = ids.indexOfFirst { decode(it) == itemId }
                val slot = if (existing >= 0) existing else ids.indexOfFirst { it <= 0 }
                if (slot < 0 || amounts[slot].toLong() + amount > client.maxItemAmount) return fail()
                ids[slot] = encode(itemId)
                amounts[slot] += amount
            } else {
                val empty = ids.indices.filter { ids[it] <= 0 }
                if (empty.size < amount) return fail()
                empty.take(amount).forEach { slot ->
                    ids[slot] = encode(itemId)
                    amounts[slot] = 1
                }
            }
            changed = true
            return true
        }

        override fun replaceAt(slot: Int, removeItemId: Int, addItemId: Int, amount: Int): Boolean {
            if (!removeAt(slot, removeItemId, amount)) return false
            return add(addItemId, amount)
        }

        fun commit() {
            if (!changed) return
            if (kind == Container.EQUIPMENT) {
                client.replaceEquipmentState(ids, amounts)
                return
            }
            val targetIds = when (kind) {
                Container.INVENTORY -> client.playerItems
                Container.BANK -> client.bankItems
                Container.EQUIPMENT -> error("Equipment is committed through Client.replaceEquipmentState")
            }
            val targetAmounts = when (kind) {
                Container.INVENTORY -> client.playerItemsN
                Container.BANK -> client.bankItemsN
                Container.EQUIPMENT -> error("Equipment is committed through Client.replaceEquipmentState")
            }
            System.arraycopy(ids, 0, targetIds, 0, ids.size)
            System.arraycopy(amounts, 0, targetAmounts, 0, amounts.size)
            when (kind) {
                Container.INVENTORY -> {
                    client.markSaveDirty(PlayerSaveSegment.INVENTORY.mask)
                    client.checkItemUpdate()
                }
                Container.BANK -> client.markSaveDirty(PlayerSaveSegment.BANK.mask)
                Container.EQUIPMENT -> error("Equipment is committed through Client.replaceEquipmentState")
            }
        }

        private fun valid(itemId: Int, amount: Int) = itemId >= 0 && amount > 0
        private fun fail(): Boolean = false.also { failed = true }
        private fun encode(itemId: Int) = if (kind == Container.EQUIPMENT) itemId else itemId + 1
        private fun decode(raw: Int) = if (raw <= 0) -1 else if (kind == Container.EQUIPMENT) raw else raw - 1
    }

    private class OfferContainer(private val client: Client) : OfferItems {
        private val staged = client.offeredItems.map { GameItem(it.id, it.amount) }.toMutableList()
        private var changed = false
        var failed = false
            private set

        override fun amountOf(itemId: Int): Int = staged.filter { it.id == itemId }.sumOf { it.amount.toLong() }
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

        override fun require(itemId: Int, amount: Int): Boolean =
            itemId >= 0 && amount > 0 && amountOf(itemId) >= amount || fail()

        override fun removeAt(slot: Int, itemId: Int, amount: Int): Boolean {
            if (slot !in staged.indices || staged[slot].id != itemId || amount <= 0 || staged[slot].amount < amount) return fail()
            val item = staged[slot]
            if (item.amount == amount) staged.removeAt(slot) else item.amount -= amount
            changed = true
            return true
        }

        override fun remove(itemId: Int, amount: Int): Boolean {
            if (!require(itemId, amount)) return false
            var remaining = amount
            var index = 0
            while (index < staged.size && remaining > 0) {
                val item = staged[index]
                if (item.id != itemId) {
                    index++
                    continue
                }
                val removed = minOf(item.amount, remaining)
                item.amount -= removed
                remaining -= removed
                if (item.amount == 0) staged.removeAt(index) else index++
            }
            changed = true
            return true
        }

        override fun add(itemId: Int, amount: Int): Boolean {
            if (itemId < 0 || amount <= 0) return fail()
            if (Server.itemManager.isStackable(itemId)) {
                val existing = staged.firstOrNull { it.id == itemId }
                if (existing != null) {
                    if (existing.amount.toLong() + amount > client.maxItemAmount) return fail()
                    existing.amount += amount
                } else staged += GameItem(itemId, amount)
            } else repeat(amount) { staged += GameItem(itemId, 1) }
            changed = true
            return true
        }

        override fun clear() {
            if (staged.isNotEmpty()) {
                staged.clear()
                changed = true
            }
        }

        override fun snapshot(): List<GameItem> = staged.map { GameItem(it.id, it.amount) }

        fun commit() {
            if (!changed) return
            client.offeredItems.clear()
            client.offeredItems.addAll(staged.map { GameItem(it.id, it.amount) })
        }

        private fun fail(): Boolean = false.also { failed = true }
    }

    companion object {
        @JvmStatic
        fun run(block: EconomyTransaction.() -> Unit): Boolean = EconomyTransaction().apply(block).commit()

        @JvmStatic
        fun transferInventoryToBank(client: Client, itemId: Int, slot: Int, amount: Int, bankItemId: Int = itemId): Boolean = run {
            if (slot !in client.playerItems.indices || client.playerItems[slot] != itemId + 1) {
                inventory(client).removeAt(-1, itemId, amount)
                return@run
            }
            inventory(client).remove(itemId, amount)
            bank(client).add(bankItemId, amount)
        }

        @JvmStatic
        fun addToInventory(client: Client, itemId: Int, amount: Int): Boolean = run {
            inventory(client).add(itemId, amount)
        }

        @JvmStatic
        fun removeFromInventory(client: Client, itemId: Int, slot: Int, amount: Int): Boolean = run {
            inventory(client).removeAt(slot, itemId, amount)
        }

        @JvmStatic
        fun replaceInventorySlot(client: Client, slot: Int, itemId: Int, amount: Int): Boolean = run {
            val inventory = inventory(client)
            val current = client.playerItems.getOrNull(slot)?.minus(1) ?: -1
            if (current >= 0) inventory.removeAt(slot, current, client.playerItemsN[slot])
            inventory.add(itemId, amount)
        }

        @JvmStatic
        fun transferBankToInventory(client: Client, itemId: Int, slot: Int, amount: Int, receivedItemId: Int = itemId): Boolean = run {
            bank(client).removeAt(slot, itemId, amount)
            inventory(client).add(receivedItemId, amount)
        }

        @JvmStatic
        fun moveInventoryToOffer(client: Client, itemId: Int, slot: Int, amount: Int): Boolean = run {
            if (slot !in client.playerItems.indices || client.playerItems[slot] != itemId + 1) {
                inventory(client).removeAt(-1, itemId, amount)
                return@run
            }
            inventory(client).remove(itemId, amount)
            offer(client).add(itemId, amount)
        }

        @JvmStatic
        fun moveOfferToInventory(client: Client, itemId: Int, slot: Int, amount: Int): Boolean = run {
            if (slot !in client.offeredItems.indices || client.offeredItems[slot].id != itemId) {
                offer(client).removeAt(-1, itemId, amount)
                return@run
            }
            offer(client).remove(itemId, amount)
            inventory(client).add(itemId, amount)
        }

        /** Transfers both trade offers as one commit or leaves both players untouched. */
        @JvmStatic
        fun settleTrade(first: Client, second: Client): Boolean = run {
            offer(first).snapshot().forEach { inventory(second).add(it.id, it.amount) }
            offer(second).snapshot().forEach { inventory(first).add(it.id, it.amount) }
            offer(first).clear()
            offer(second).clear()
        }
    }
}
