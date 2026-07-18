package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.engine.systems.net.PacketGameplayFacade;
import net.dodian.uber.game.engine.systems.net.WalkRequest;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {248, 164, 98})
public final class WalkingListener implements PacketListener {

    private static final int WALK_PACKET_SIZE = 5;
    private static final int MIN_WORLD_COORD = 0;
    private static final int MAX_WORLD_COORD = 16382;
    @Override
    public void handle(Client client, GamePacket packet) {
        int opcode = packet.opcode();
        int size   = packet.size();

        ByteBuf buf = packet.payload();
        if (size != WALK_PACKET_SIZE || buf.readableBytes() != WALK_PACKET_SIZE) {
            rejectMalformedWalkPacket(client, opcode, size, -1, -1, "Tarnish walk payload must be exactly five bytes");
            return;
        }
        if (client.mapRegionX < 0 || client.mapRegionY < 0) {
            rejectMalformedWalkPacket(client, opcode, size, -1, -1, "map region not initialized");
            return;
        }

        // Tarnish writes target X as LE, target Y as LE+A, then the negated run byte.
        // It sends only the destination; pathfinding remains server-owned.
        WalkRequest request = decodeTarnishDestination(opcode, buf);
        int firstStepXAbs = request.getFirstStepXAbs();
        int firstStepYAbs = request.getFirstStepYAbs();
        if (!isValidWorldCoordinate(firstStepXAbs) || !isValidWorldCoordinate(firstStepYAbs)) {
            rejectMalformedWalkPacket(client, opcode, size, firstStepXAbs, firstStepYAbs, "first step out of world bounds");
            return;
        }
        PacketGameplayFacade.handleWalk(client, request);
    }

    static WalkRequest decodeTarnishDestination(int opcode, ByteBuf buf) {
        int targetX = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
        int targetY = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
        boolean running = ByteBufReader.readSignedByte(buf, ValueType.NEGATE) == 1;
        return new WalkRequest(opcode, targetX, targetY, running, new int[]{0}, new int[]{0});
    }

    private static boolean isValidWorldCoordinate(int value) {
        return value >= MIN_WORLD_COORD && value <= MAX_WORLD_COORD;
    }

    private static void rejectMalformedWalkPacket(
            Client client,
            int opcode,
            int packetSize,
            int firstStepXAbs,
            int firstStepYAbs,
            String reason
    ) {
        PacketGameplayFacade.rejectMalformedWalk(client, opcode, packetSize, firstStepXAbs, firstStepYAbs, reason);
    }
}
