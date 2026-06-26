package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;

/**
 * This is used for various in-game effects like spell impacts, teleport effects, etc.
 */
public class StillGraphic implements OutgoingPacket {

    private final int id;
    private final Position position;
    private final int height;
    private final int time;
    private final boolean showAll;

    
    public StillGraphic(int id, Position position, int height, int time, boolean showAll) {
        this.id = id;
        this.position = position;
        this.height = height;
        this.time = time;
        this.showAll = showAll;
    }

    @Override
    public void send(Client client) {
        int baseX = (position.getX() >> 3) << 3;
        int baseY = (position.getY() >> 3) << 3;

        client.send(new SetMap(new Position(baseX, baseY)));

        int localX = position.getX() - baseX;
        int localY = position.getY() - baseY;
        int offsetByte = (localX << 4) | localY;

        ByteMessage message = ByteMessage.message(4, MessageType.FIXED);
        message.put(offsetByte); 
        message.putShort(id);    
        message.put(height);     
        message.putShort(time);

        client.send(message);
    }
}