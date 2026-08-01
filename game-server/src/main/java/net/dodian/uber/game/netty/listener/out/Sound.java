package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public final class Sound implements OutgoingPacket {

    private final int soundId;
    private final int volume;
    private final int delay;

    public Sound(int soundId, int volume, int delay) {
        this.soundId = soundId;
        this.volume = volume;
        this.delay = delay;
    }

    public Sound(int soundId, int delay) {
        this(soundId, 4, delay);
    }

    public Sound(int soundId) {
        this(soundId, 4, 0);
    }

    public int soundId() {
        return soundId;
    }

    public int volume() {
        return volume;
    }

    public int delay() {
        return delay;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.Sound(soundId, volume, delay).encode());
    }

}
