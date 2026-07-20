package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

/**
 * This is used for various in-game projectiles like arrows, spells, etc.
 */
public class Projectile implements OutgoingPacket {

    private final Position casterPosition;
    private final int offsetY;
    private final int offsetX;
    private final int speed;
    private final int gfxMoving;
    private final int startHeight;
    private final int endHeight;
    private final int targetIndex;
    private final int begin;
    private final int slope;
    private final int initDistance;
    private Object targetObject = null;

    public Projectile(Position casterPosition, int offsetY, int offsetX, int speed, int gfxMoving, int startHeight, int endHeight, int targetIndex, int begin, int slope, int initDistance) {
        this.casterPosition = casterPosition;
        this.offsetY = offsetY;
        this.offsetX = offsetX;
        this.speed = speed;
        this.gfxMoving = gfxMoving;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.targetIndex = targetIndex;
        this.begin = begin;
        this.slope = slope;
        this.initDistance = initDistance;
    }

    public Projectile(Position casterPosition, Object targetObject, int speed, int gfxMoving, int startHeight, int endHeight, int targetIndex, int begin, int slope, int initDistance) {
        this.casterPosition = casterPosition;
        this.targetObject = targetObject;
        this.speed = speed;
        this.gfxMoving = gfxMoving;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.targetIndex = targetIndex;
        this.begin = begin;
        this.slope = slope;
        this.initDistance = initDistance;
        this.offsetY = 0;
        this.offsetX = 0;
    }

    @Override
    public void send(Client client) {
        int baseX = (casterPosition.getX() >> 3) << 3;
        int baseY = (casterPosition.getY() >> 3) << 3;

        client.send(new SetMap(new Position(baseX, baseY)));

        int localX = casterPosition.getX() - baseX;
        int localY = casterPosition.getY() - baseY;
        int offsetByte = (localX << 4) | localY;

        int finalOffsetX;
        int finalOffsetY;

        if (targetObject == null) {
            finalOffsetX = offsetX;
            finalOffsetY = offsetY;
        } else {
            Position targetPos;
            if (targetObject instanceof net.dodian.uber.game.model.entity.Entity) {
                targetPos = ((net.dodian.uber.game.model.entity.Entity) targetObject).getPosition();
            } else if (targetObject instanceof Position) {
                targetPos = (Position) targetObject;
            } else {
                targetPos = casterPosition;
            }
            finalOffsetX = targetPos.getX() - casterPosition.getX();
            finalOffsetY = targetPos.getY() - casterPosition.getY();
        }

        client.send(new TarnishOutboundPackets.Projectile(
            offsetByte, finalOffsetX, finalOffsetY, targetIndex, gfxMoving,
            startHeight, endHeight, begin, speed, slope, initDistance
        ).encode());
    }
}
