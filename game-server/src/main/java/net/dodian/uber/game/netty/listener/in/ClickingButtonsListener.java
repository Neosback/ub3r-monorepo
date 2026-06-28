package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketHandler;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.netty.listener.PacketListenerManager;
import net.dodian.uber.game.engine.event.GameEventBus;
import net.dodian.uber.game.events.widget.ButtonClickEvent;
import net.dodian.uber.game.engine.systems.interaction.PlayerTickThrottleService;
import net.dodian.uber.game.ui.buttons.ButtonClickLoggingService;
import net.dodian.uber.game.ui.buttons.ButtonClickRequest;
import net.dodian.uber.game.ui.buttons.InterfaceButtonBinding;
import net.dodian.uber.game.ui.buttons.InterfaceButtonRegistry;
import net.dodian.uber.game.ui.buttons.InterfaceButtonService;
import net.dodian.uber.game.engine.systems.net.PacketButtonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PacketHandler(opcode = 185)
public class ClickingButtonsListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(ClickingButtonsListener.class);

    static {
        PacketListenerManager.register(185, new ClickingButtonsListener());
        PacketListenerManager.register(186, new ClickingButtonsListener());
    }

    @Override
    public void handle(Client client, GamePacket packet) {
        int packetSize = packet.size();
        if (packetSize < 4) {
            logger.warn("ClickingButtons opcode 185 with unexpected size {} from {}", packetSize, client.getPlayerName());
            return;
        }

        ByteBuf payload = packet.payload();
        int actionButton = payload.readInt();
        int actionIndex = -1;
        if (packet.opcode() == 186 && payload.isReadable()) {
            actionIndex = payload.readUnsignedByte();
        }
        PacketButtonService.recordLastActionIndex(client, actionIndex);

        if (PacketButtonService.isSmeltingInterfaceActive(client)) {
            logger.warn(
                    "Smelting button click buttonId={} actionIndex={} size={} iface={} player={}",
                    actionButton,
                    actionIndex,
                    packetSize,
                    client.activeInterfaceId,
                    client.getPlayerName()
            );
        }

        if (!client.validClient || !PlayerTickThrottleService.tryAcquireMs(client, PlayerTickThrottleService.BUTTON_GENERAL, 600L)) {
            return;
        }

        PacketButtonService.prepareAction(client, actionButton);

        if (actionButton >= 36731 && actionButton <= 36769) {
            int index = (actionButton - 36731) / 2;
            if (client.viewingAccountServices) {
                client.handleAccountServicesRowClick(index);
            } else {
                handleModcpRowClick(client, index);
            }
            return;
        }

        InterfaceButtonBinding resolvedBinding = InterfaceButtonRegistry.INSTANCE.resolve(client, actionButton, actionIndex);
        ButtonClickRequest request = new ButtonClickRequest(
                client,
                actionButton,
                actionIndex,
                client.activeInterfaceId,
                resolvedBinding != null ? resolvedBinding.getInterfaceId() : -1,
                resolvedBinding != null ? resolvedBinding.getComponentId() : -1,
                resolvedBinding != null ? resolvedBinding.getComponentKey() : "raw:" + actionButton
        );

        if (GameEventBus.postWithResult(new ButtonClickEvent(request))) {
            ButtonClickLoggingService.logClick(request, packet.opcode(), true);
            return;
        }

        if (InterfaceButtonService.tryHandle(client, actionButton, actionIndex)) {
            ButtonClickLoggingService.logClick(request, packet.opcode(), true);
            return;
        }

        if (PacketButtonService.isSmeltingInterfaceActive(client)) {
            logger.warn(
                    "Unhandled smelting button buttonId={} actionIndex={} iface={} player={}",
                    actionButton,
                    actionIndex,
                    client.activeInterfaceId,
                    client.getPlayerName()
            );
        }

        ButtonClickLoggingService.logClick(request, packet.opcode(), false);
    }

    private void handleModcpRowClick(Client client, int index) {
        if (client.playerRights < 1) {
            client.sendMessage("You do not have permission to execute this staff command.");
            return;
        }
        if (index < 0 || index >= client.modcpPlayerList.size()) {
            client.sendMessage("No player in this slot.");
            return;
        }
        String targetName = client.modcpPlayerList.get(index);
        client.managingName = targetName;

        // Show player details on the right side of the interface
        Client other = null;
        for (int i = 0; i < net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players.length; i++) {
            net.dodian.uber.game.model.entity.player.Player p = net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players[i];
            if (p != null && p.playerName.equalsIgnoreCase(targetName.trim())) {
                other = (Client) p;
                break;
            }
        }

        client.loadAndShowModcpDetails(targetName, other != null, other != null ? other.connectedFrom : "Offline");

        // Show player options dialogue
        client.modcpDialogState = 1;
        client.showPlayerOption(new String[]{
            "Manage " + targetName,
            "Teleport Actions",
            "Moderator Actions",
            "Check Containers",
            "Cancel"
        });
    }
}