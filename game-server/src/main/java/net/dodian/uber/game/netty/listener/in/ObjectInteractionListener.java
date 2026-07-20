package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.cache.objects.GameObjectData;
import net.dodian.cache.objects.GameObjectDef;
import net.dodian.uber.game.engine.metrics.PacketRejectTelemetry;
import net.dodian.uber.game.engine.systems.net.PacketRejectReason;
import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.interaction.ItemOnObjectIntent;
import net.dodian.uber.game.engine.systems.interaction.ObjectClickIntent;
import net.dodian.uber.game.engine.systems.interaction.scheduler.InteractionTaskScheduler;
import net.dodian.uber.game.engine.systems.interaction.scheduler.ObjectInteractionTask;
import net.dodian.uber.game.engine.systems.net.PacketMagicService;
import net.dodian.uber.game.engine.systems.net.PacketObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {132, 252, 70, 234, 228, 192, 35})
public class ObjectInteractionListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(ObjectInteractionListener.class);
    private static final int MIN_COORD = -1;
    private static final int MAX_COORD = 16382;
    @Override
    public void handle(Client client, GamePacket packet) {
        switch (packet.opcode()) {
            case 132:
                handleClick(client, packet, 1);
                return;
            case 252:
                handleClick(client, packet, 2);
                return;
            case 70:
                handleClick(client, packet, 3);
                return;
            case 234:
                handleClick(client, packet, 4);
                return;
            case 228:
                handleClick(client, packet, 5);
                return;
            case 192:
                handleItemOnObject(client, packet);
                return;
            case 35:
                handleMagicOnObject(client, packet);
                return;
            default:
                logger.warn(
                    "ObjectInteractionListener got unexpected opcode={} for player={}",
                    packet.opcode(),
                    client.getPlayerName()
                );
        }
    }

    private void handleClick(Client client, GamePacket packet, int option) {
        DecodedObjectClick decoded = decodeClickPacket(packet, option);
        if (decoded == null) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            logger.warn("Object click packet decode failed option={} player={}", option, client.getPlayerName());
            return;
        }

        final int objectId = decoded.objectId();
        final int objectX = decoded.objectX();
        final int objectY = decoded.objectY();
        if (!isValidObjectClick(objectId, objectX, objectY)) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.INVALID_COORDINATE);
            logger.debug(
                "Rejected invalid object click player={} option={} objectId={} x={} y={}",
                client.getPlayerName(),
                option,
                objectId,
                objectX,
                objectY
            );
            return;
        }

        PacketObjectService.handleObjectClick(client, packet.opcode(), option, objectId, objectX, objectY);
    }

    private void handleItemOnObject(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < 12) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.SHORT_PAYLOAD);
            logger.warn("ItemOnObject packet too short for player={}", client.getPlayerName());
            return;
        }
        if (buf.readableBytes() > 12) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            logger.warn("ItemOnObject packet too large size={} for player={}", buf.readableBytes(), client.getPlayerName());
            return;
        }

        net.dodian.uber.game.netty.game.decode.TarnishPackets.ItemOnObject msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.ItemOnObject.decode(buf);
        if (msg == null) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            return;
        }
        final int interfaceId = msg.interfaceId();
        final int objectId = msg.objectId();
        final int objectY = msg.objectY();
        final int itemSlot = msg.slot();
        final int objectX = msg.objectX();
        final int itemId = msg.itemId();
        if (!isValidObjectClick(objectId, objectX, objectY)) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.INVALID_COORDINATE);
            logger.debug(
                "Rejected invalid item-on-object coordinates player={} objectId={} x={} y={}",
                client.getPlayerName(),
                objectId,
                objectX,
                objectY
            );
            return;
        }
        if (itemSlot < 0) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.INVALID_SLOT);
            logger.debug("Rejected invalid item-on-object slot player={} itemSlot={}", client.getPlayerName(), itemSlot);
            return;
        }
        if (itemId < 0) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.INVALID_ID);
            logger.debug(
                "Rejected invalid item-on-object id player={} itemId={} objectId={} x={} y={} itemSlot={}",
                client.getPlayerName(),
                itemId,
                objectId,
                objectX,
                objectY,
                itemSlot
            );
            return;
        }

        PacketObjectService.handleItemOnObject(client, packet.opcode(), interfaceId, objectId, objectX, objectY, itemSlot, itemId);
    }

    private void handleMagicOnObject(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < 8) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.SHORT_PAYLOAD);
            logger.warn("MagicOnObject packet too short for player={}", client.getPlayerName());
            return;
        }
        if (buf.readableBytes() > 8) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            logger.warn("MagicOnObject packet too large size={} for player={}", buf.readableBytes(), client.getPlayerName());
            return;
        }

        net.dodian.uber.game.netty.game.decode.TarnishPackets.MagicOnObject msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.MagicOnObject.decode(buf);
        if (msg == null) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            return;
        }
        final int objectX = msg.x();
        final int spellId = msg.spellId();
        final int objectY = msg.y();
        final int objectId = msg.objectId();
        if (!isValidObjectClick(objectId, objectX, objectY)) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.INVALID_COORDINATE);
            logger.debug(
                "Rejected invalid magic-on-object coordinates player={} objectId={} x={} y={}",
                client.getPlayerName(),
                objectId,
                objectX,
                objectY
            );
            return;
        }
        if (spellId < 0) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.INVALID_ID);
            logger.debug(
                "Rejected invalid magic-on-object spell id player={} spellId={} objectId={} x={} y={}",
                client.getPlayerName(),
                spellId,
                objectId,
                objectX,
                objectY
            );
            return;
        }

        PacketMagicService.handleMagicOnObject(client, packet.opcode(), objectX, objectY, objectId, spellId);
    }

    private DecodedObjectClick decodeClickPacket(GamePacket packet, int option) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.ObjectClick click =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.ObjectClick.decode(packet.opcode(), packet.payload());
        if (click == null) {
            return null;
        }
        return new DecodedObjectClick(click.objectId(), click.x(), click.y());
    }

    private static boolean isValidObjectClick(int objectId, int objectX, int objectY) {
        return objectId >= 0 &&
            objectX >= MIN_COORD && objectX <= MAX_COORD &&
            objectY >= MIN_COORD && objectY <= MAX_COORD;
    }
    private static final class DecodedObjectClick {
        private final int objectId;
        private final int objectX;
        private final int objectY;

        private DecodedObjectClick(int objectId, int objectX, int objectY) {
            this.objectId = objectId;
            this.objectX = objectX;
            this.objectY = objectY;
        }

        private int objectId() {
            return objectId;
        }

        private int objectX() {
            return objectX;
        }

        private int objectY() {
            return objectY;
        }
    }
}