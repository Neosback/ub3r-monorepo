package net.dodian.game.event.events

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.event.GameEvent
import net.dodian.game.systems.ui.buttons.ButtonClickRequest

data class ButtonClickEvent(
    val request: ButtonClickRequest,
) : GameEvent {
    val client: Client
        get() = request.client

    val buttonId: Int
        get() = request.rawButtonId
}
