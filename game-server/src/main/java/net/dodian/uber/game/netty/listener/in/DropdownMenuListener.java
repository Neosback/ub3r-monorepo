package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;

@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {255})
public class DropdownMenuListener implements PacketListener {
    @Override
    public void handle(Client client, GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.DropdownSelect msg = net.dodian.uber.game.netty.game.decode.TarnishPackets.DropdownSelect.decode(packet.payload());
        if (msg == null) {
            return;
        }
        int identification = msg.componentId();
        int value = msg.value() & 0xFF;

        if (identification < 0 || value < 0) {
            return;
        }

        if (identification == 50205) { // Entity attack option
            client.setEntityAttackOption(value);
        }
    }
}
