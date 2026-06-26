package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import io.netty.channel.ChannelFutureListener;


public class Logout implements OutgoingPacket {

    private static final int LOGOUT_OPCODE = 109;

    @Override
    public void send(Client client) {
        ByteMessage bm = ByteMessage.message(LOGOUT_OPCODE);
        
        if (client.getChannel() != null && client.getChannel().isActive()) {
            client.getChannel().writeAndFlush(bm).addListener(ChannelFutureListener.CLOSE);
        } else {
            bm.release();
        }
    }
}