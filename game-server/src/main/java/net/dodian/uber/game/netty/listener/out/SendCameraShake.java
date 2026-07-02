package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

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
        ByteMessage message = ByteMessage.message(35);
        message.put(slot);
        message.put(randomAmount);
        message.put(sineAmplitude);
        message.put(sineSpeed);
        client.send(message);
    }

    private static int clampByte(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
