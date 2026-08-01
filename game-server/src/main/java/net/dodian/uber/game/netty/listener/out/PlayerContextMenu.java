package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class PlayerContextMenu implements OutgoingPacket {

    private final int commandSlot;
    private final boolean enabled;
    private final String command;

    public PlayerContextMenu(int commandSlot, boolean enabled, String command) {
        this.commandSlot = commandSlot;
        this.enabled = enabled;
        this.command = command;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.PlayerContextMenu(commandSlot, enabled, command).encode());
    }
}