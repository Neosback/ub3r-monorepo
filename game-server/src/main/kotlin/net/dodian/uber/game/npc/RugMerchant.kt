package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.skill.agility.AgilityTravel

internal object RugMerchant : NpcScript("RugMerchant", 17, 19, 20, 22) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            examine = "Proud owner of carpet co"
            hitpoints = 18
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        onOption("talk-to") {
            action {
                DialogueService.start(client) {
                    npcChat(npc.id, DialogueEmote.NEARLY_CRYING, "Hello there Traveler.", "Do you fancy taking a carpet ride?", "It will cost 5000 coins.")
                    options(
                        title = "Where can your carpet take me to?",
                        DialogueOption("Show destinations") {
                            val destinations = when (npc.id) {
                                17 -> arrayOf("Pollnivneach", "Nardah", "Bedabin Camp")
                                19 -> arrayOf("Pollnivneach", "Nardah", "Sophanem")
                                20 -> arrayOf("Nardah", "Bedabin Camp", "Sophanem")
                                22 -> arrayOf("Pollnivneach", "Sophanem", "Bedabin Camp")
                                else -> arrayOf("Cancel", "Cancel", "Cancel")
                            }
                            options(
                                title = "Carpet rides cost 5k coins.",
                                DialogueOption(destinations[0]) {
                                    action { c -> travel(c, npc.id, 0) }
                                    finish()
                                },
                                DialogueOption(destinations[1]) {
                                    action { c -> travel(c, npc.id, 1) }
                                    finish()
                                },
                                DialogueOption(destinations[2]) {
                                    action { c -> travel(c, npc.id, 2) }
                                    finish()
                                },
                                DialogueOption("Cancel") { finish() },
                            )
                        },
                        DialogueOption("No, thank you.") {
                            playerChat(DialogueEmote.ANGRY1, "No, thank you.")
                            finish()
                        },
                    )
                }
                true
            }
        }

        onOption("second") {
            action {
                DialogueService.start(client) {
                    npcChat(npc.id, DialogueEmote.NEARLY_CRYING, "Hello there Traveler.", "Do you fancy taking a carpet ride?", "It will cost 5000 coins.")
                    options(
                        title = "Where can your carpet take me to?",
                        DialogueOption("Show destinations") {
                            val destinations = when (npc.id) {
                                17 -> arrayOf("Pollnivneach", "Nardah", "Bedabin Camp")
                                19 -> arrayOf("Pollnivneach", "Nardah", "Sophanem")
                                20 -> arrayOf("Nardah", "Bedabin Camp", "Sophanem")
                                22 -> arrayOf("Pollnivneach", "Sophanem", "Bedabin Camp")
                                else -> arrayOf("Cancel", "Cancel", "Cancel")
                            }
                            options(
                                title = "Carpet rides cost 5k coins.",
                                DialogueOption(destinations[0]) {
                                    action { c -> travel(c, npc.id, 0) }
                                    finish()
                                },
                                DialogueOption(destinations[1]) {
                                    action { c -> travel(c, npc.id, 1) }
                                    finish()
                                },
                                DialogueOption(destinations[2]) {
                                    action { c -> travel(c, npc.id, 2) }
                                    finish()
                                },
                                DialogueOption("Cancel") { finish() },
                            )
                        },
                        DialogueOption("No, thank you.") {
                            playerChat(DialogueEmote.ANGRY1, "No, thank you.")
                            finish()
                        },
                    )
                }
                true
            }
        }

        spawns(
            spawn(3181, 3045, profile = profile("rug_merchant.bedabin_camp")),
            spawn(3401, 2918, profile = profile("rug_merchant.nardah")),
            spawn(3348, 3002, profile = profile("rug_merchant.pollnivneach")),
            spawn(3287, 2814, profile = profile("rug_merchant.jalsavrah_pyramid")),
        )
    }

    private fun travel(client: Client, npcId: Int, choice: Int) {
        var missing = 5000
        val amount = client.getInvAmt(995).toLong() + client.getBankAmt(995)
        if (amount >= 5000L) {
            if (client.getInvAmt(995) >= missing) {
                client.deleteItem(995, missing)
            } else {
                missing -= client.getInvAmt(995)
                client.deleteItem(995, client.getInvAmt(995))
            }
            if (missing > 0) {
                client.deleteItemBank(995, missing)
            }
            client.checkItemUpdate()
        } else {
            DialogueService.start(client) {
                npcChat(
                    npcId,
                    DialogueEmote.EVIL3,
                    "You do not have enough coins to do this!",
                    "You are currently missing ${missing - amount} coins.",
                )
                finish()
            }
            return
        }

        val carpet = AgilityTravel(client)
        when (npcId) {
            17 -> carpet.sophanem(choice)
            19 -> carpet.bedabinCamp(choice)
            20 -> carpet.pollnivneach(choice)
            22 -> carpet.nardah(choice)
        }
    }
}