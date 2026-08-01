package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.event.GameEventBus;
import net.dodian.uber.game.events.widget.DialogueContinueEvent;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {40})
public class DialogueListener implements PacketListener {
    @Override
    public void handle(Client client, GamePacket packet) {
        // No fields to decode; discard payload if present.
        ByteBuf buf = packet.payload();
        if (buf.isReadable()) {
            buf.skipBytes(buf.readableBytes());
        }

        GameEventBus.post(new DialogueContinueEvent(client));
    }
}