package net.dodian.uber.game.model.item.transaction

import net.dodian.uber.game.model.entity.player.Client

/**
 * Java-callable atomic bank <-> inventory transfers built on the [Transaction] engine.
 * Unlike a plain [OfferTransactions] transfer, deposit/withdraw can change the item id between
 * the two sides (noted/unnoted conversion) and support placeholder retention, so each is
 * expressed as an explicit `delete` + `insert` pair rather than a single `transfer` query.
 */
object BankTransactions {

    /**
     * Withdraws [amount] of [itemId] from [client]'s bank slot [slot] into their inventory as
     * [receivedItemId] (noted/unnoted variant). If [retainPlaceholder] is set and the slot is
     * emptied, a placeholder is left behind instead of clearing the slot.
     */
    @JvmStatic
    fun withdraw(
        client: Client,
        itemId: Int,
        slot: Int,
        amount: Int,
        receivedItemId: Int = itemId,
        retainPlaceholder: Boolean = false,
    ): Boolean {
        val bank = client.selectBank()
        val inv = client.selectInv()
        return transaction(bank, inv) {
            delete {
                from = bank.transactionInv
                obj = itemId
                strictSlot = slot
                strictCount = amount
            }
            // This server's placeholders are just the same item id left behind at zero
            // count (not a distinct template-linked item like RSMod's `placehold` models),
            // so retention is applied directly once the slot is confirmed fully emptied.
            if (retainPlaceholder && bank.transactionInv[slot] == null) {
                bank.transactionInv[slot] = TransactionObj(itemId, 0)
            }
            insert {
                into = inv.transactionInv
                obj = receivedItemId
                strictCount = amount
            }
        }
    }

    /**
     * Deposits [amount] of [itemId] from [client]'s inventory slot [slot] into their bank as
     * [bankItemId] (unnoted variant).
     */
    @JvmStatic
    fun deposit(client: Client, itemId: Int, slot: Int, amount: Int, bankItemId: Int = itemId): Boolean {
        val inv = client.selectInv()
        val bank = client.selectBank()
        val existing = inv.transactionInv[slot] ?: return false
        if (existing.id != itemId) return false
        return transaction(inv, bank) {
            delete {
                from = inv.transactionInv
                obj = itemId
                strictCount = amount
            }
            insert {
                into = bank.transactionInv
                obj = bankItemId
                strictCount = amount
            }
        }
    }
}
