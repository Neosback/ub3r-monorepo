package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;

/**
 * Sends opcode 27 (SEND_ENTER_AMOUNT) to prompt the client for a numeric input.
 * Tarnish packet structure: a string prompt followed by a short-ADD maximum length.
 */
public class SendFrame27 implements OutgoingPacket {

    private final String title;
    private final int inputLength;

    public SendFrame27() {
        this("Enter amount:", 10);
    }

    public SendFrame27(String title) {
        this(title, 10);
    }

    public SendFrame27(String title, int inputLength) {
        this.title = title != null ? title : "Enter amount:";
        this.inputLength = inputLength;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(27, MessageType.VAR_SHORT);
        message.putString(title);
        message.putShort(inputLength, ValueType.ADD);
        client.send(message);
    }
}
