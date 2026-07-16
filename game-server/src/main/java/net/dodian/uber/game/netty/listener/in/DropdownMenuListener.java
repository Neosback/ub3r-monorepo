package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;

@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {255})
public class DropdownMenuListener implements PacketListener {
    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < 5) {
            return;
        }

        int identification = buf.readInt();
        int value = buf.readByte() & 0xFF;

        if (identification < 0 || value < 0) {
            return;
        }

        if (identification == 50205) { // Entity attack option
            client.setEntityAttackOption(value);
        }
    }
}
