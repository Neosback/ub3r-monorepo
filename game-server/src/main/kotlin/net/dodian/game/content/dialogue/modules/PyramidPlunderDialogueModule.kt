package net.dodian.game.content.dialogue.modules

import net.dodian.game.systems.ui.dialogue.DialogueService
import net.dodian.game.systems.ui.dialogue.core.DialogueIds
import net.dodian.game.systems.ui.dialogue.core.DialogueRenderRegistry
import net.dodian.game.systems.ui.dialogue.core.DialogueRenderModule
import net.dodian.game.systems.ui.dialogue.core.DialogueUi

object PyramidPlunderDialogueModule : DialogueRenderModule {
    override fun register(builder: DialogueRenderRegistry.Builder) {
        builder.handle(DialogueIds.Misc.PYRAMID_EXIT) { c ->
            DialogueUi.showPlayerOption(c, arrayOf("Exit pyramid plunder?", "Yes", "No"))
            DialogueService.setDialogueSent(c, true)
            true
        }
    }
}
