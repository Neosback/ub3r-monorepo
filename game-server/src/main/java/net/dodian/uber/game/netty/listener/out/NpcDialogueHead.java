package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;

public class NpcDialogueHead implements OutgoingPacket {

    private final int mainFrame;
    private final int subFrame;

    public NpcDialogueHead(int mainFrame, int subFrame) {
        this.mainFrame = mainFrame;
        this.subFrame = subFrame;
    }

    @Override
    public void send(Client client) {
        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.NpcDialogueHead(mainFrame, subFrame).encode());
    }

}