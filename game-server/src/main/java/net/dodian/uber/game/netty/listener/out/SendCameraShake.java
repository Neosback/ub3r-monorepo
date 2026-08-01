package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendCameraShake implements OutgoingPacket {

    private final int slot;
    private final int randomAmount;
    private final int sineAmplitude;
    private final int sineSpeed;

    public SendCameraShake(int slot, int randomAmount, int sineAmplitude, int sineSpeed) {
        if (slot < 0 || slot > 4) {
            throw new IllegalArgumentException("Camera shake slot must be 0..4. Got " + slot);
        }
        this.slot = clampByte(slot);
        this.randomAmount = clampByte(randomAmount);
        this.sineAmplitude = clampByte(sineAmplitude);
        this.sineSpeed = clampByte(sineSpeed);
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.CameraShake(slot, randomAmount, sineAmplitude, sineSpeed).encode());
    }

    private static int clampByte(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
