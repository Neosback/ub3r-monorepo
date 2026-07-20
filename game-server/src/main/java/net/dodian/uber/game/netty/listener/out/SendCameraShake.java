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
        this.slot = slot;
        this.randomAmount = randomAmount;
        this.sineAmplitude = sineAmplitude;
        this.sineSpeed = sineSpeed;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.CameraShake(slot, randomAmount, sineAmplitude, sineSpeed).encode());
    }

}
