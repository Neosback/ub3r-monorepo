package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.netty.listener.PacketListenerManager;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;

/**
 * Handles text syntax input (opcode 60).
 * Delegates bank-search flag checks and state mutation to PacketBankingService.
 */
public class SyntaxInputListener implements PacketListener {

    static {
        PacketListenerManager.register(60, new SyntaxInputListener());
    }

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < 8) {
            return;
        }

        long nameLong = buf.readLong();
        String input = net.dodian.utilities.Names.longToPlayerName(nameLong).trim();

        if (client.accountPasswordState == 1) {
            client.accountPasswordState = 0;
            client.verifyCurrentPassword(input);
            return;
        }
        if (client.accountPasswordState == 2) {
            client.accountPasswordState = 0;
            client.changePassword(input);
            return;
        }

        if (client.modcpSearchPendingInput) {
            client.modcpSearchPendingInput = false;
            client.openModcp(input);
            return;
        }

        if (net.dodian.uber.game.engine.systems.net.PacketSocialService.handlePendingNameInput(client, nameLong)) {
            return;
        }

        if (PacketBankingService.hasPendingBankSearch(client)) {
            PacketBankingService.applyPendingBankSearch(client, input);
        }
    }
}
