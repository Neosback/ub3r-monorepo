package net.dodian.game.content.interfaces.appearance

import net.dodian.game.model.UpdateFlag
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces
import net.dodian.game.systems.ui.buttons.InterfaceButtonContent
import net.dodian.game.systems.ui.buttons.buttonBinding

object AppearanceInterfaceButtons : InterfaceButtonContent {
    override val bindings =
        listOf(
            buttonBinding(-1, 0, "appearance.confirm", AppearanceComponents.confirmButtons) { client, _ ->
                client.send(RemoveInterfaces())
                client.updateFlags.setRequired(UpdateFlag.APPEARANCE, true)
                true
            }
        )
}
