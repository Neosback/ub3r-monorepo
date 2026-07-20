package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.player.skills.Skill;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class RefreshSkill implements OutgoingPacket {

    private final Skill skill;
    private final int level;
    private final int maxLevel;
    private final int experience;

    public RefreshSkill(Skill skill, int level, int maxLevel, int experience) {
        this.skill = skill;
        this.level = level;
        this.maxLevel = maxLevel;
        this.experience = experience;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.RefreshSkill(skill.getId(), experience, level).encode());
    }
}
