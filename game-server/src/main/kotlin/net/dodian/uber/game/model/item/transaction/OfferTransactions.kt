package net.dodian.uber.game.model.item.transaction

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.GameItem

/**
 * Java-callable atomic operations over trade/duel stake offers ([Client.offeredItems]),
 * built on the [Transaction] engine via [SelectInv]/[transaction]. Every function here either
 * fully applies or leaves every involved container (which may span two different [Client]s)
 * completely untouched — there is no partial-trade or partial-refund outcome.
 */
object OfferTransactions {

    /** Moves [amount] of [itemId] from [client]'s inventory slot [fromSlot] into their offer. */
    @JvmStatic
    fun stakeToOffer(client: Client, itemId: Int, fromSlot: Int, amount: Int): Boolean {
        val inv = client.selectInv()
        val offer = client.selectOffer()
        val obj = inv.transactionInv[fromSlot] ?: return false
        if (obj.id != itemId) return false
        return transaction(inv, offer) {
            transfer {
                from = inv.transactionInv
                into = offer.transactionInv
                this.fromSlot = fromSlot
                count = amount
            }
        }
    }

    /** Moves [amount] of [itemId] from [client]'s offer slot [fromSlot] back into their inventory. */
    @JvmStatic
    fun offerToInventory(client: Client, itemId: Int, fromSlot: Int, amount: Int): Boolean {
        val offer = client.selectOffer()
        val inv = client.selectInv()
        val obj = offer.transactionInv[fromSlot] ?: return false
        if (obj.id != itemId) return false
        return transaction(offer, inv) {
            transfer {
                from = offer.transactionInv
                into = inv.transactionInv
                this.fromSlot = fromSlot
                count = amount
            }
        }
    }

    /**
     * Swaps [first]'s and [second]'s staged offers into each other's inventories and clears
     * both offers, as one atomic commit. If either inventory can't fit the incoming stake, the
     * whole trade is rolled back and neither player's containers are touched.
     */
    @JvmStatic
    fun settleTrade(first: Client, second: Client): Boolean {
        val firstOffer = first.selectOffer()
        val secondOffer = second.selectOffer()
        val firstInv = first.selectInv()
        val secondInv = second.selectInv()
        return transaction(firstOffer, secondOffer, firstInv, secondInv) {
            drainOffer(firstOffer, secondInv)
            drainOffer(secondOffer, firstInv)
        }
    }

    /** Returns every distinct client's staged offer to their own inventory, as one commit. */
    @JvmStatic
    fun refundOffers(vararg clients: Client): Boolean {
        val distinct = clients.distinct()
        val offers = distinct.map { it.selectOffer() }
        val invs = distinct.map { it.selectInv() }
        val containers = (offers + invs).toTypedArray()
        return transaction(*containers) {
            for (i in distinct.indices) {
                drainOffer(offers[i], invs[i])
            }
        }
    }

    /**
     * Moves both [winner]'s and [loser]'s staked offers into [winner]'s inventory and clears
     * both offers, as one atomic commit. Fails safely (both offers left staged, untouched) if
     * the winner's inventory can't fit the combined payout.
     */
    @JvmStatic
    fun settleDuelPayout(winner: Client, loser: Client): Boolean {
        val winnerOffer = winner.selectOffer()
        val loserOffer = loser.selectOffer()
        val winnerInv = winner.selectInv()
        return transaction(winnerOffer, loserOffer, winnerInv) {
            drainOffer(winnerOffer, winnerInv)
            drainOffer(loserOffer, winnerInv)
        }
    }

    /** Transfers every occupied slot of [offer] into [dest], strict (throws on any shortfall). */
    private fun Transaction<GameItem?>.drainOffer(offer: SelectInv, dest: SelectInv) {
        val image = offer.transactionInv.image
        for (slot in image.indices) {
            val obj = image[slot] ?: continue
            transfer {
                from = offer.transactionInv
                into = dest.transactionInv
                fromSlot = slot
                count = obj.count
            }
        }
    }
}
