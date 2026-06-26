package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;

public final class SendExpCounter implements OutgoingPacket {
    private final int skill;
    private final int experience;
    private final boolean counter;

    public SendExpCounter(int skill, int experience, boolean counter) {
        this.skill = skill;
        this.experience = experience;
        this.counter = counter;
    }

    public SendExpCounter(int experience) {
        this(99, experience, true);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(127);
        message.put(skill);
        message.putInt(experience);
        message.put(counter ? 1 : 0);
        client.send(message);
    }
}