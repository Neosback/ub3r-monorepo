package net.dodian.uber.game.ui

import net.dodian.uber.game.engine.systems.net.PacketSocialService
import net.dodian.uber.game.ui.buttons.InterfaceButtonContent
import net.dodian.uber.game.ui.buttons.buttonBinding

object SocialInterface : InterfaceButtonContent {
    private val addFriendButtons = intArrayOf(5068)
    private val removeFriendButtons = intArrayOf(5069)
    private val addIgnoreButtons = intArrayOf(5718)
    private val removeIgnoreButtons = intArrayOf(5719)

    override val bindings =
        listOf(
            buttonBinding(-1, 0, "social.friend.add", addFriendButtons) { client, _ ->
                PacketSocialService.requestAddFriend(client)
                true
            },
            buttonBinding(-1, 1, "social.friend.remove", removeFriendButtons) { client, _ ->
                PacketSocialService.requestRemoveFriend(client)
                true
            },
            buttonBinding(-1, 2, "social.ignore.add", addIgnoreButtons) { client, _ ->
                PacketSocialService.requestAddIgnore(client)
                true
            },
            buttonBinding(-1, 3, "social.ignore.remove", removeIgnoreButtons) { client, _ ->
                PacketSocialService.requestRemoveIgnore(client)
                true
            },
        )
}
