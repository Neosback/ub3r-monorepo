package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Saniboch : NpcScript("Saniboch", 2345) {
    private data class NpcReply(
        val emote: DialogueEmote = DialogueEmote.DEFAULT,
        val lines: Array<String>,
    )

    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            hitpoints = 0
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        onOption("talk-to") {
            action {
                DialogueService.start(client) {
                    if (client.checkUnlock(0)) {
                        npcChat(npc.id, DialogueEmote.DEFAULT, "You can enter freely, no need to pay me anything.")
                        finish()
                    } else {
                        npcChat(npc.id, DialogueEmote.DEFAULT, "Hello!", "Are you looking to enter my dungeon?", "You have to pay to enter.", "You can also pay a one time fee.")
                        options(
                            title = "Select an option",
                            DialogueOption("Enter fee") {
                                options(
                                    title = "Select a payment option",
                                    DialogueOption("Ship ticket") {
                                        val reply = shipTicketReply(client)
                                        npcChat(npc.id, reply.emote, *reply.lines)
                                        finish()
                                    },
                                    DialogueOption("Coins") {
                                        val reply = coinPaymentReply(client)
                                        npcChat(npc.id, reply.emote, *reply.lines)
                                        finish()
                                    },
                                )
                            },
                            DialogueOption("Permanent unlock") {
                                val reply = permanentUnlockReply(client)
                                npcChat(npc.id, reply.emote, *reply.lines)
                                finish()
                            },
                            DialogueOption("Nevermind") {
                                playerChat(DialogueEmote.DEFAULT, "I do not want anything.")
                                finish()
                            },
                        )
                    }
                }
                true
            }
        }

        onOption("second") {
            action {
                DialogueService.start(client) {
                    if (client.checkUnlock(0)) {
                        npcChat(npc.id, DialogueEmote.DEFAULT, "You can enter freely, no need to pay me anything.")
                        finish()
                    } else {
                        npcChat(npc.id, DialogueEmote.DEFAULT, "Hello!", "Are you looking to enter my dungeon?", "You have to pay to enter.", "You can also pay a one time fee.")
                        options(
                            title = "Select an option",
                            DialogueOption("Enter fee") {
                                options(
                                    title = "Select a payment option",
                                    DialogueOption("Ship ticket") {
                                        val reply = shipTicketReply(client)
                                        npcChat(npc.id, reply.emote, *reply.lines)
                                        finish()
                                    },
                                    DialogueOption("Coins") {
                                        val reply = coinPaymentReply(client)
                                        npcChat(npc.id, reply.emote, *reply.lines)
                                        finish()
                                    },
                                )
                            },
                            DialogueOption("Permanent unlock") {
                                val reply = permanentUnlockReply(client)
                                npcChat(npc.id, reply.emote, *reply.lines)
                                finish()
                            },
                            DialogueOption("Nevermind") {
                                playerChat(DialogueEmote.DEFAULT, "I do not want anything.")
                                finish()
                            },
                        )
                    }
                }
                true
            }
        }
    }

    private fun shipTicketReply(client: Client): NpcReply {
        if (client.checkUnlockPaid(0) > 0) {
            return NpcReply(lines = arrayOf("You have already paid me.", "Please step into my dungeon."))
        }
        if (client.getInvAmt(621) > 0 || client.getBankAmt(621) > 0) {
            client.addUnlocks(0, "1", if (client.checkUnlock(0)) "1" else "0")
            if (client.getInvAmt(621) > 0) client.deleteItem(621, 1) else client.deleteItemBank(621, 1)
            client.checkItemUpdate()
            return NpcReply(lines = arrayOf("You can now step into the dungeon."))
        }
        return NpcReply(emote = DialogueEmote.DISTRESSED, lines = arrayOf("You need a ship ticket to enter my dungeon!"))
    }

    private fun coinPaymentReply(client: Client): NpcReply {
        val amount = client.getInvAmt(995).toLong() + client.getBankAmt(995)
        val total = 300_000
        if (amount >= total) {
            client.addUnlocks(0, "1", if (client.checkUnlock(0)) "1" else "0")
            val remain = total - client.getInvAmt(995)
            client.deleteItem(995, total)
            if (remain > 0) client.deleteItemBank(995, remain)
            return NpcReply(lines = arrayOf("You can now step into the dungeon."))
        }
        return NpcReply(
            emote = DialogueEmote.DISTRESSED,
            lines = arrayOf("You need at least ${total - amount} more coins to enter my dungeon!"),
        )
    }

    private fun permanentUnlockReply(client: Client): NpcReply {
        if (client.checkUnlock(0)) {
            return NpcReply(lines = arrayOf("You can already enter freely."))
        }
        val maximumTickets = 10
        val minimumTicket = 1
        val ticketValue = 300_000
        var missing = (maximumTickets - minimumTicket) * ticketValue
        if (!client.playerHasItem(621, minimumTicket)) {
            return NpcReply(lines = arrayOf("You need a minimum of $minimumTicket ship ticket", "to unlock permanent!"))
        }

        missing -= (client.getInvAmt(621) - minimumTicket) * ticketValue
        val reply =
            if (missing > 0) {
                if (client.getInvAmt(995) >= missing) {
                    client.deleteItem(621, kotlin.math.min(client.getInvAmt(621), maximumTickets))
                    client.deleteItem(995, missing)
                    client.addUnlocks(0, client.checkUnlockPaid(0).toString(), "1")
                    NpcReply(lines = arrayOf("Thank you for the payment.", "You may enter freely into my dungeon."))
                } else {
                    NpcReply(lines = arrayOf("You do not have enough coins to do this!"))
                }
            } else {
                client.deleteItem(621, maximumTickets)
                client.addUnlocks(0, client.checkUnlockPaid(0).toString(), "1")
                NpcReply(lines = arrayOf("Thank you for the ship tickets.", "You may enter freely into my dungeon."))
            }
        client.checkItemUpdate()
        return reply
    }
}