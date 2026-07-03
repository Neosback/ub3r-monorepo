package net.dodian.uber.game.netty.listener.out

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.codec.ByteMessage
import net.dodian.uber.game.netty.codec.ByteOrder
import net.dodian.uber.game.netty.listener.OutgoingPacket

class SendMoveComponent(private val x: Int, private val y: Int, private val id: Int) : OutgoingPacket {
    override fun send(client: Client) {
        val message = ByteMessage.message(70)
        message.putShort(x)
        message.putShort(y, ByteOrder.LITTLE)
        message.putShort(id, ByteOrder.LITTLE)
        client.send(message)
    }
}
