package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;
import io.netty.channel.ChannelFutureListener;

public class Logout implements OutgoingPacket {

    @Override
    public void send(Client client) {
        ByteMessage bm = new TarnishOutboundPackets.Logout().encode();
        if (client.getChannel() != null && client.getChannel().isActive()) {
            client.getChannel().writeAndFlush(bm).addListener(ChannelFutureListener.CLOSE);
        } else {
            bm.release();
        }
    }
}