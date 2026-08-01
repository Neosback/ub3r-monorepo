package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class PrivateMessage implements OutgoingPacket {

    private final long recipient;
    private final int rights;
    private final byte[] message;
    private final int size;
    private final int messageId;

    public PrivateMessage(long recipient, int rights, byte[] message, int size, int messageId) {
        this.recipient = recipient;
        this.rights = rights;
        this.message = message;
        this.size = size;
        this.messageId = messageId;
    }

    @Override
    public void send(Client client) {
        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.PrivateMessage(recipient, rights, message, size, messageId).encode());
    }
}