package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketHandler;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.netty.listener.PacketListenerManager;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PacketHandler(opcode = 214)
public class MoveItemsListener implements PacketListener {

    static {
        PacketListenerManager.register(214, new MoveItemsListener());
    }

    private static final Logger logger = LoggerFactory.getLogger(MoveItemsListener.class);
    private static final int MIN_PAYLOAD_BYTES = 9;

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < MIN_PAYLOAD_BYTES) {
            return;
        }

        int interfaceId = ByteBufReader.readInt(buf);
        // Client writes writeNegatedByte(mode) = (byte)(-mode); recover original mode.
        int rawModeByte = buf.readUnsignedByte();
        int mode = (-rawModeByte) & 0xFF;
        int itemFrom = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
        int itemTo = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);

        if (client.playerRights >= 2) {
            client.println_debug("MoveItems: iface=" + interfaceId + " mode=" + mode + " from=" + itemFrom + " to=" + itemTo);
        }

        logger.debug("MoveItems: iface={} mode={} from={} to={}", interfaceId, mode, itemFrom, itemTo);

        // mode=2 means the player dragged a bank item onto a tab button; itemTo is the tab index.
        if (mode == 2) {
            PacketBankingService.handleTabCreation(client, interfaceId, itemFrom, itemTo);
            return;
        }

        PacketBankingService.handleMoveItems(client, interfaceId, itemFrom, itemTo);
    }
}
