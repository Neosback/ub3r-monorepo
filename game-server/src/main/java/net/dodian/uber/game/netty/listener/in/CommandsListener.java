package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketSocialService;
import java.nio.charset.StandardCharsets;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {103})
public class CommandsListener implements PacketListener {
    @Override
    public void handle(Client client, GamePacket packet) {
        String command = decodeCommand(packet);

        if (command.isEmpty()) {
            return;
        }

        logCommand(client, command);
        PacketSocialService.handleCommand(client, command);
    }

    /**
     * Decodes the command string from the game packet payload.
     * The string is terminated by a newline (10) or null (0) character.
     * @param packet The incoming game packet.
     * @return The decoded command string.
     */
    private String decodeCommand(GamePacket packet) {
        ByteBuf buf = packet.payload();
        int readable = Math.min(packet.size(), buf.readableBytes());
        int startIndex = buf.readerIndex();
        int length = 0;
        while (length < readable) {
            int value = buf.getByte(startIndex + length) & 0xFF;
            if (value == 10 || value == 0) {
                break;
            }
            length++;
        }
        String command = buf.readCharSequence(length, StandardCharsets.ISO_8859_1).toString();
        int remaining = readable - length;
        if (remaining > 0) {
            buf.skipBytes(remaining);
        }
        return command;
    }

    /**
     * Logs the command for debugging purposes, filtering sensitive commands.
     * @param client The client executing the command.
     * @param command The command being executed.
     */
    private void logCommand(Client client, String command) {
        if (!command.contains("password") && !command.contains("unstuck") && !command.startsWith("examine")) {
            client.println_debug("playerCommand: " + command);
        }
    }

}