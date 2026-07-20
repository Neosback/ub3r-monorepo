package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class CreateGroundItem implements OutgoingPacket {

    private final GameItem item;
    private final Position position;

    public CreateGroundItem(GameItem item, Position position) {
        this.item = item;
        this.position = position;
    }

    @Override
    public void send(Client client) {
        client.send(new SetMap(position));
        client.send(new TarnishOutboundPackets.CreateGroundItem(item.getId(), item.getAmount()).encode());
    }

}
