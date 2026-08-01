package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class Animation2 implements OutgoingPacket {

    private final int animationId;
    private final Position position;
    private final int height;
    private final int delay;

    
    public Animation2(int animationId, Position position, int height, int delay) {
        this.animationId = animationId;
        this.position = position;
        this.height = height;
        this.delay = delay;
    }

    @Override
    public void send(Client client) {
        int baseX = (position.getX() >> 3) << 3;
        int baseY = (position.getY() >> 3) << 3;

        client.send(new SetMap(new Position(baseX, baseY)));

        int localX = position.getX() - baseX;
        int localY = position.getY() - baseY;
        int offsetByte = (localX << 4) | localY;

        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.Animation2(offsetByte, animationId, height, delay).encode());
    }
}