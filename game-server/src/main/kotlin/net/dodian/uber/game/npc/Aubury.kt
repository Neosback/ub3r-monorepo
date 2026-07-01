package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.objects.travel.EssenceMineTravel
import net.dodian.uber.game.shop.ShopId
import net.dodian.utilities.Utils

private val AuburyVarrock = profile("aubury.varrock")
private val AuburyYanille = profile("aubury.yanille")

internal object Aubury : NpcFamily by npcFamily("Aubury", 10681, block = {
    cache {
        name = "Aubury"
    }

    runtime {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = Aubury::talkTo)
        trade(handler = Aubury::trade)
        third("teleport", Aubury::teleport)
    }

    spawns {
        spawn(2594, 3104, profile = AuburyYanille)
        spawn(3253, 3402, profile = AuburyVarrock)
    }
}) {

    fun talkTo(client: Client, npc: Npc): Boolean {
        DialogueService.start(client) {
            npcChat(npc.id, DialogueEmote.EVIL1, "Do you want to buy some runes?")
            options(
                title = "Select an Option",
                DialogueOption("Yes please!") {
                    finishThen {
                        it.openUpShopRouted(ShopId.AUBURYS_MAGIC_STORE.id)
                    }
                },
                DialogueOption("No thank you, then.") {
                    playerChat(DialogueEmote.DEFAULT, "Oh it's a rune shop. No thank you, then.")
                    npcChat(npc.id, DialogueEmote.DEFAULT, "Well, if you find someone who does want runes, send them my way.")
                },
            )
        }
        return true
    }

    fun trade(client: Client, npc: Npc): Boolean {
        client.openUpShopRouted(ShopId.AUBURYS_MAGIC_STORE.id)
        return true
    }

    fun teleport(client: Client, npc: Npc): Boolean {
        if (npc.interactionProfile == AuburyVarrock.key) {
            return EssenceMineTravel.sendToEssenceMine(client)
        }

        client.transport(Position(3086 + Utils.random(2), 3488 + Utils.random(2), 0))
        client.sendMessage("Welcome to Edgeville!")
        return true
    }
}
