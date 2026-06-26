package net.dodian.uber.game.ui.dialogue

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.SetInterfaceConfig
import net.dodian.uber.game.netty.listener.out.NpcDialogueHead
import net.dodian.uber.game.netty.listener.out.PlayerDialogueHead
import net.dodian.uber.game.netty.listener.out.SendString

object DialogueUi {

    @JvmStatic
    fun showPlayerOption(client: Client, text: Array<String>) = with(client) {
        var base = 2459
        if (text.size == 4) base = 2469
        if (text.size == 5) base = 2480
        if (text.size == 6) base = 2492

        send(SetInterfaceConfig(1, base + 4 + text.size - 1))
        send(SetInterfaceConfig(0, base + 7 + text.size - 1))
        for (i in text.indices) {
            send(SendString(text[i], base + 1 + i))
        }
        sendChatboxInterface(base)
    }

    @JvmStatic
    fun showNpcChat(client: Client, npcId: Int, emote: Int, text: Array<String>) = with(client) {
        var base = 4882
        if (text.size == 2) base = 4887
        if (text.size == 3) base = 4893
        if (text.size == 4) base = 4900

        sendInterfaceAnimation(base + 1, emote)
        send(SendString(GetNpcName(npcId), base + 2))
        for (i in text.indices) {
            send(SendString(text[i], base + 3 + i))
        }
        send(SendString("Click here to continue", base + 3 + text.size))
        send(NpcDialogueHead(npcId, base + 1))
        sendChatboxInterface(base)
    }

    @JvmStatic
    fun showPlayerChat(client: Client, text: Array<String>, emote: Int) = with(client) {
        var base = 968
        if (text.size == 2) base = 973
        if (text.size == 3) base = 979
        if (text.size == 4) base = 986

        send(PlayerDialogueHead(base + 1))
        sendInterfaceAnimation(base + 1, emote)
        send(SendString(playerName, base + 2))
        for (i in text.indices) {
            send(SendString(text[i], base + 3 + i))
        }
        sendChatboxInterface(base)
    }

    @JvmStatic
    fun showStatement(client: Client, text: Array<String>) = with(client) {
        when (text.size) {
            1 -> {
                send(SendString(text[0], 357))
                sendChatboxInterface(356)
            }
            2 -> {
                send(SendString(text[0], 360))
                send(SendString(text[1], 361))
                sendChatboxInterface(359)
            }
            3 -> {
                send(SendString(text[0], 364))
                send(SendString(text[1], 365))
                send(SendString(text[2], 366))
                sendChatboxInterface(363)
            }
            4 -> {
                send(SendString(text[0], 369))
                send(SendString(text[1], 370))
                send(SendString(text[2], 371))
                send(SendString(text[3], 372))
                sendChatboxInterface(368)
            }
            5 -> {
                send(SendString(text[0], 375))
                send(SendString(text[1], 376))
                send(SendString(text[2], 377))
                send(SendString(text[3], 378))
                send(SendString(text[4], 379))
                sendChatboxInterface(374)
            }
            else -> {
                send(SendString(text[0], 375))
                send(SendString(text[1], 376))
                send(SendString(text[2], 377))
                send(SendString(text[3], 378))
                send(SendString(text[4], 379))
                sendChatboxInterface(374)
            }
        }
    }
}