package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {214})
public class MoveItemsListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(MoveItemsListener.class);
    private static final int PAYLOAD_BYTES = 7;

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() != PAYLOAD_BYTES) {
            return;
        }

        net.dodian.uber.game.netty.game.decode.TarnishPackets.MoveItems msg = net.dodian.uber.game.netty.game.decode.TarnishPackets.MoveItems.decode(buf);
        if (msg == null) {
            return;
        }
        int interfaceId = msg.interfaceId();
        int mode = msg.mode();
        int itemFrom = msg.fromSlot();
        int itemTo = msg.toSlot();

        if (client.playerRights >= 2) {
            client.println_debug("MoveItems: iface=" + interfaceId + " mode=" + mode + " from=" + itemFrom + " to=" + itemTo);
        }

        logger.debug("MoveItems: iface={} mode={} from={} to={}", interfaceId, mode, itemFrom, itemTo);

        if (mode == 2) {
            PacketBankingService.handleTabCreation(client, interfaceId, itemFrom, itemTo);
            return;
        }

        PacketBankingService.handleMoveItems(client, interfaceId, itemFrom, itemTo, mode);
    }

}
