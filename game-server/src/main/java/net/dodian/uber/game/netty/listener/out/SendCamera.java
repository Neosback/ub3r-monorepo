package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendCamera implements OutgoingPacket {

    private final int x;
    private final int y;
    private final int z;
    private int speed;
    private int angle;
    private int sp1;
    private int sp2;
    private final String mode;

    public SendCamera(String mode, int x, int y, int z, int speed, int angle) {
        this.mode = mode;
        this.x = x;
        this.y = y;
        this.z = z;
        this.speed = speed;
        this.angle = angle;
    }

    public SendCamera(String mode, int x, int y, int z, int speed1, int speed2, String dummy) {
        this.mode = mode;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sp1 = speed1;
        this.sp2 = speed2;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = null;
        switch (mode) {
            case "update":
            case "spin":
                message = new TarnishOutboundPackets.CameraPosition(x / 64, y / 64, z, speed, angle).encode();
                break;
            case "rotation":
                message = new TarnishOutboundPackets.CameraRotation(x, y, z, sp1, sp2).encode();
                break;
            case "location":
                break;
        }

        if (message != null) {
            client.send(message);
        }
    }

}