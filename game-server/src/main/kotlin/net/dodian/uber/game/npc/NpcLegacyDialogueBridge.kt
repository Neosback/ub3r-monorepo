package net.dodian.uber.game.npc

import net.dodian.uber.game.content.social.dialogue.core.DialogueRegistry
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object DukeHoracio {
    val npcIds: IntArray = net.dodian.uber.game.content.npcs.DukeHoracio.npcIds

    fun onFirstClick(client: Client, npc: Npc): Boolean =
        net.dodian.uber.game.content.npcs.DukeHoracio.onFirstClick(client, npc)

    fun registerLegacyDialogues(builder: DialogueRegistry.Builder) =
        net.dodian.uber.game.content.npcs.DukeHoracio.registerLegacyDialogues(builder)
}

internal object PartyPete {
    val npcIds: IntArray = net.dodian.uber.game.content.npcs.PartyPete.npcIds

    fun onFirstClick(client: Client, npc: Npc): Boolean =
        net.dodian.uber.game.content.npcs.PartyPete.onFirstClick(client, npc)

    fun registerLegacyDialogues(builder: DialogueRegistry.Builder) =
        net.dodian.uber.game.content.npcs.PartyPete.registerLegacyDialogues(builder)
}

internal object Watcher {
    val npcIds: IntArray = net.dodian.uber.game.content.npcs.Watcher.npcIds

    fun registerLegacyDialogues(builder: DialogueRegistry.Builder) =
        net.dodian.uber.game.content.npcs.Watcher.registerLegacyDialogues(builder)
}

internal object UnknownNpc1597 {
    val npcIds: IntArray = net.dodian.uber.game.content.npcs.unknown.UnknownNpc1597.npcIds

    fun registerLegacyDialogues(builder: DialogueRegistry.Builder) =
        net.dodian.uber.game.content.npcs.unknown.UnknownNpc1597.registerLegacyDialogues(builder)
}

internal object HerbloreNpcDialogue {
    fun openDecantDoseOptions(client: Client, npcId: Int) =
        net.dodian.uber.game.content.npcs.HerbloreNpcDialogue.openDecantDoseOptions(client, npcId)

    fun openHerbCleaner(client: Client, npcId: Int) =
        net.dodian.uber.game.content.npcs.HerbloreNpcDialogue.openHerbCleaner(client, npcId)

    fun openUnfinishedPotionMaker(client: Client, npcId: Int) =
        net.dodian.uber.game.content.npcs.HerbloreNpcDialogue.openUnfinishedPotionMaker(client, npcId)

    fun showBatchResultAndContinue(client: Client, npcId: Int, firstLine: String, secondLine: String) =
        net.dodian.uber.game.content.npcs.HerbloreNpcDialogue.showBatchResultAndContinue(client, npcId, firstLine, secondLine)
}

internal object SlayerMasterDialogue {
    fun startIntro(client: Client, npcId: Int) =
        net.dodian.uber.game.content.npcs.SlayerMasterDialogue.startIntro(client, npcId)

    fun assignTask(client: Client, npcId: Int) =
        net.dodian.uber.game.content.npcs.SlayerMasterDialogue.assignTask(client, npcId)

    fun showCurrentTask(client: Client) =
        net.dodian.uber.game.content.npcs.SlayerMasterDialogue.showCurrentTask(client)

    fun showResetCountPrompt(client: Client) =
        net.dodian.uber.game.content.npcs.SlayerMasterDialogue.showResetCountPrompt(client)
}
