package net.dodian.uber.game.engine.systems.interaction.ui

import net.dodian.uber.game.model.entity.player.Client

object TradeDuelStateMachine {
    @JvmStatic
    fun advanceTradeStageOne(client: Client, other: Client): Boolean {
        if (!client.inTrade || client.tradeConfirmed) {
            return true
        }
        client.tradeConfirmed = true
        if (other.tradeConfirmed) {
            if (other.hasTradeSpace() || client.hasTradeSpace()) {
                client.sendMessage(client.failer)
                other.sendMessage(client.failer)
                TradeDuelSessionService.closeOpenTrade(client)
                return true
            }
            client.confirmScreen()
            other.confirmScreen()
            return true
        }
        client.sendString("Waiting for other player...", 3431)
        if (client.validClient(client.trade_reqId)) {
            other.sendString("Other player has accepted", 3431)
        }
        return true
    }

    @JvmStatic
    fun advanceTradeStageTwo(client: Client, other: Client): Boolean {
        if (!client.inTrade || !client.tradeConfirmed || !other.tradeConfirmed || client.tradeConfirmed2) {
            return true
        }
        client.tradeConfirmed2 = true
        if (other.tradeConfirmed2) {
            client.giveItems()
            other.giveItems()
        } else {
            other.sendString("Other player has accepted.", 3535)
            client.sendString("Waiting for other player...", 3535)
        }
        return true
    }

    @JvmStatic
    fun advanceDuelStageOne(client: Client, other: Client): Boolean {
        if (!client.inDuel || client.duelConfirmed) {
            return true
        }
        if (!canAttackWithDuelRules(client)) {
            client.sendString("You don't have weapons/spells for rules!", 31009)
            client.sendMessage("You don't have the right equipment to attack with the enabled combat styles!")
            return true
        }
        if (!canAttackWithDuelRules(other)) {
            client.sendString("Opponent doesn't have weapons/spells!", 31009)
            client.sendMessage("Your opponent doesn't have the right equipment to attack with the enabled combat styles!")
            return true
        }
        client.duelConfirmed = true
        if (!other.duelConfirmed) {
            client.sendString("Waiting for other player...", 31009)
            other.sendString("Other player has accepted.", 31009)
            return true
        }

        if (client.duelRule[0] && client.duelRule[1] && client.duelRule[2]) {
            TradeDuelSessionService.closeOpenDuel(client)
            client.sendMessage("At least one combat style must be enabled!")
            other.sendMessage("At least one combat style must be enabled!")
            return true
        }
        if (client.hasEnoughSpace() || other.hasEnoughSpace()) {
            client.sendMessage(client.failer)
            other.sendMessage(client.failer)
            TradeDuelSessionService.closeOpenDuel(client)
            return true
        }

        client.canOffer = false
        client.confirmDuel()
        other.confirmDuel()
        return true
    }

    @JvmStatic
    fun advanceDuelStageTwo(client: Client, other: Client): Boolean {
        if (!client.inDuel || client.duelConfirmed2) {
            return true
        }
        client.canOffer = false
        client.duelConfirmed2 = true
        if (other.duelConfirmed2) {
            client.removeEquipment()
            other.removeEquipment()
            if (!canAttackWithDuelRules(client) || !canAttackWithDuelRules(other)) {
                val msg = "You don't have the right equipment to attack with the enabled combat styles!"
                client.sendMessage(msg)
                other.sendMessage(msg)
                client.declineDuel()
                other.declineDuel()
                return true
            }
            client.startDuel()
            other.startDuel()
        } else {
            client.sendString("Waiting for other player...", 31526)
            other.sendString("Other player has accepted", 31526)
        }
        return true
    }

    @JvmStatic
    fun canAttackWithDuelRules(player: Client): Boolean {
        val noMelee = player.duelRule[1]
        val noRanged = player.duelRule[0]
        val noMagic = player.duelRule[2]

        if (!noMelee) return true
        if (!noRanged && player.usingBow) return true
        if (!noMagic && (player.magicId >= 0 || (player.hasStaff() && player.autocast_spellIndex >= 0))) return true
        return false
    }
}