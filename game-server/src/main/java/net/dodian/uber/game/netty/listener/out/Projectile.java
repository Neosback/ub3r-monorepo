package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;

/**
 * This is used for various in-game projectiles like arrows, spells, etc.
 */
public class Projectile implements OutgoingPacket {

    private final Position casterPosition;
    private final int offsetY;
    private final int offsetX;
    private final int angle;
    private final int speed;
    private final int gfxMoving;
    private final int startHeight;
    private final int endHeight;
    private final int targetIndex;
    private final int begin;
    private final int slope;
    private final int initDistance;

    
    public Projectile(Position casterPosition, int offsetY, int offsetX, int angle, int speed,
                     int gfxMoving, int startHeight, int endHeight, int targetIndex,
                     int begin, int slope, int initDistance) {
        this.casterPosition = casterPosition;
        this.offsetY = offsetY;
        this.offsetX = offsetX;
        this.angle = angle;
        this.speed = speed;
        this.gfxMoving = gfxMoving;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.targetIndex = targetIndex;
        this.begin = begin;
        this.slope = slope;
        this.initDistance = initDistance;
    }

    @Override
    public void send(Client client) {
        int baseX = (casterPosition.getX() >> 3) << 3;
        int baseY = (casterPosition.getY() >> 3) << 3;

        client.send(new SetMap(new Position(baseX, baseY)));

        int localX = casterPosition.getX() - baseX;
        int localY = casterPosition.getY() - baseY;
        int offsetByte = (localX << 4) | localY;

        ByteMessage message = ByteMessage.message(117, MessageType.FIXED);
        message.put(offsetByte);       
        message.put(offsetX);

        message.put(offsetY);

        message.putShort(targetIndex);

        message.putShort(gfxMoving);   
        message.put(startHeight);

        message.put(endHeight);

        message.putShort(begin);

        message.putShort(speed);

        message.put(slope);

        message.put(initDistance);

        client.send(message);
    }
}