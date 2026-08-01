package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;

/**
 * Opcode 71 – assigns an interface (form) to one of the sidebar tabs.
 *
 * Legacy layout:
 *   createFrame(71)
 *   writeWord(form)
 *   writeByteA(menuId)   // value + 128
 */
public class SetSidebarInterface implements OutgoingPacket {

    private final int menuId;
    private final int form;

    public SetSidebarInterface(int menuId, int form) {
        this.menuId = menuId;
        this.form = form;
    }

    @Override
    public void send(Client client) {
        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.SetSidebarInterface(menuId, form).encode());
    }
}