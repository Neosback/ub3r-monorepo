package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SetTabInterface implements OutgoingPacket {

    private final int mainFrame;
    private final int subFrame;

    public SetTabInterface(int mainFrame, int subFrame) {
        this.mainFrame = mainFrame;
        this.subFrame = subFrame;
    }

    public int mainFrame() {
        return mainFrame;
    }

    public int subFrame() {
        return subFrame;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetInventoryInterface(mainFrame, subFrame).encode());
    }
}
