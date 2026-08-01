package net.dodian.uber.game.netty.listener.out

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.codec.ByteMessage
import net.dodian.uber.game.netty.listener.OutgoingPacket

class SendSpecialAmount(private val amount: Int) : OutgoingPacket {
    override fun send(client: Client) {
        val message = ByteMessage.message(137)
        message.put(amount)
        client.send(message)
    }
}
