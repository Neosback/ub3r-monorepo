package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.engine.event.GameEventBus;
import net.dodian.uber.game.engine.metrics.PacketRejectTelemetry;
import net.dodian.uber.game.engine.systems.net.PacketRejectReason;
import net.dodian.uber.game.events.item.ItemExamineEvent;
import net.dodian.uber.game.events.npc.NpcExamineEvent;
import net.dodian.uber.game.events.objects.ObjectExamineEvent;
import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.dodian.uber.game.engine.config.DotEnvKt.getGameWorldId;

@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {150})
public final class ExamineListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(ExamineListener.class);
    private static final int EXPECTED_PAYLOAD_SIZE = 7;
    @Override
    public void handle(Client client, GamePacket packet) {
        try {
            ByteBuf buf = packet.payload();

            if (buf.readableBytes() < EXPECTED_PAYLOAD_SIZE) {
                PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.SHORT_PAYLOAD);
                logger.warn("Examine packet too small (size={}) from {}", buf.readableBytes(), client.getPlayerName());
                return;
            }
            if (buf.readableBytes() > EXPECTED_PAYLOAD_SIZE) {
                PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
                logger.warn("Examine packet too large (size={}) from {}", buf.readableBytes(), client.getPlayerName());
                return;
            }

            net.dodian.uber.game.netty.game.decode.TarnishPackets.Examine msg = net.dodian.uber.game.netty.game.decode.TarnishPackets.Examine.decode(buf);
            if (msg == null) {
                return;
            }
            int type = msg.type();
            int id = msg.id();
            int param1 = msg.param1();
            int param2 = msg.param2();

            if (getGameWorldId() > 1) {
                logger.debug("Examine: Type={}, Id={}, param1={}, param2={}", type, id, param1, param2);
            }

            if (type == 0) { // Interface Item Examine
                boolean handled = GameEventBus.postWithResult(new ItemExamineEvent(client, param2, id));
                if (!handled) {
                    client.examineItem(client, param2, id);
                }
            } else if (type == 1) { // NPC Examine
                boolean handled = GameEventBus.postWithResult(new NpcExamineEvent(client, id));
                if (!handled) {
                    client.examineNpc(client, id);
                }
            } else if (type == 2) { // Ground Item Examine
                boolean handled = GameEventBus.postWithResult(new ItemExamineEvent(client, id, 1));
                if (!handled) {
                    client.examineItem(client, id, 1);
                }
            } else if (type == 3) { // Object Examine
                Position objectPosition = client.getPosition();
                boolean handled = GameEventBus.postWithResult(new ObjectExamineEvent(client, id, objectPosition));
                if (!handled) {
                    client.examineObject(client, id, objectPosition);
                }
            }
        } catch (Exception e) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            logger.error("Error handling Examine packet for {}", client.getPlayerName(), e);
        }
    }
}