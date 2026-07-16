package net.dodian.uber.game.netty.listener;

import net.dodian.uber.game.engine.systems.net.PacketRegistrationReport;
import net.dodian.uber.game.netty.listener.in.WalkingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages packet listener registration.
 */
public final class PacketListenerManager {

    private static final Logger logger = LoggerFactory.getLogger(PacketListenerManager.class);
    
    private static final PacketRepository repository = PacketRepository.getInstance();
    
    private static volatile PacketRegistrationReport registrationReport;

    private static final String[] LISTENER_CLASSES = {
            "net.dodian.uber.game.netty.listener.in.KeepAliveListener",
            "net.dodian.uber.game.netty.listener.in.WalkingListener",
            "net.dodian.uber.game.netty.listener.in.ChangeRegionListener",
            "net.dodian.uber.game.netty.listener.in.ChangeAppearanceListener",
            "net.dodian.uber.game.netty.listener.in.ChatListener",
            "net.dodian.uber.game.netty.listener.in.UseItemOnPlayerListener",
            "net.dodian.uber.game.netty.listener.in.FollowPlayerListener",
            "net.dodian.uber.game.netty.listener.in.ItemOnGroundItemListener",
            "net.dodian.uber.game.netty.listener.in.UseItemOnNpcListener",
            "net.dodian.uber.game.netty.listener.in.ItemOnItemListener",
            "net.dodian.uber.game.netty.listener.in.ClickItem2Listener",
            "net.dodian.uber.game.netty.listener.in.ClickItemListener",
            "net.dodian.uber.game.netty.listener.in.ObjectInteractionListener",
            "net.dodian.uber.game.netty.listener.in.NpcInteractionListener",
            "net.dodian.uber.game.netty.listener.in.MagicOnPlayerListener",
            "net.dodian.uber.game.netty.listener.in.TradeListener",
            "net.dodian.uber.game.netty.listener.in.TradeRequestListener",
            "net.dodian.uber.game.netty.listener.in.SendPrivateMessageListener",
            "net.dodian.uber.game.netty.listener.in.MagicOnItemsListener",
            "net.dodian.uber.game.netty.listener.in.RemoveItemListener",
            "net.dodian.uber.game.netty.listener.in.AttackPlayerListener",
            "net.dodian.uber.game.netty.listener.in.WearItemListener",
            "net.dodian.uber.game.netty.listener.in.FocusChangeListener",
            "net.dodian.uber.game.netty.listener.in.CommandsListener",
            "net.dodian.uber.game.netty.listener.in.ExamineListener",
            "net.dodian.uber.game.netty.listener.in.NpcDropTableListener",
            "net.dodian.uber.game.netty.listener.in.ClickingButtonsListener",
            "net.dodian.uber.game.netty.listener.in.AddFriendListener",
            "net.dodian.uber.game.netty.listener.in.AddIgnoreListener",
            "net.dodian.uber.game.netty.listener.in.DuelRequestListener",
            "net.dodian.uber.game.netty.listener.in.RemoveFriendListener",
            "net.dodian.uber.game.netty.listener.in.RemoveIgnoreListener",
            "net.dodian.uber.game.netty.listener.in.ClickItem3Listener",
            "net.dodian.uber.game.netty.listener.in.DialogueListener",
            "net.dodian.uber.game.netty.listener.in.ClickingStuffListener",
            "net.dodian.uber.game.netty.listener.in.DropItemListener",
            "net.dodian.uber.game.netty.listener.in.PickUpGroundItemListener",
            "net.dodian.uber.game.netty.listener.in.BankAllListener",
            "net.dodian.uber.game.netty.listener.in.Bank5Listener",
            "net.dodian.uber.game.netty.listener.in.BankX1Listener",
            "net.dodian.uber.game.netty.listener.in.BankX2Listener",
            "net.dodian.uber.game.netty.listener.in.BankAllButOneListener",
            "net.dodian.uber.game.netty.listener.in.BankWithdrawRememberedXListener",
            "net.dodian.uber.game.netty.listener.in.MagicOnNpcListener",
            "net.dodian.uber.game.netty.listener.in.Bank10Listener",
            "net.dodian.uber.game.netty.listener.in.MouseClicksListener",
            "net.dodian.uber.game.netty.listener.in.MoveItemsListener",
            "net.dodian.uber.game.netty.listener.in.UpdateChatListener",
            "net.dodian.uber.game.netty.listener.in.SyntaxInputListener",
            "net.dodian.uber.game.netty.listener.in.InputFieldListener",
            "net.dodian.uber.game.netty.listener.in.BankTabCreationListener",
            "net.dodian.uber.game.netty.listener.in.DropdownMenuListener"
    };

    public static synchronized PacketRegistrationReport initialize() {
        if (registrationReport != null) {
            return registrationReport;
        }
        try {
            logger.debug("Starting to register packet listeners...");
            for (String listenerClass : LISTENER_CLASSES) {
                Class.forName(listenerClass);
            }
            
            repository.registerNoOp(77);
            repository.registerNoOp(202);
            repository.registerNoOp(86);
            repository.registerNoOp(229);
            

            PacketRegistrationReport report = buildRegistrationReport();
            validateCriticalOpcodesOrThrow(report);
            repository.lock();
            registrationReport = report;
            logger.info("packet_listeners_ready registered={} duplicates={}",
                    report.getRegisteredCount(), report.getDuplicateOverwriteCount());
            return report;
        } catch (Throwable e) {
            logger.error("Failed to register packet listeners", e);
            throw new IllegalStateException("Packet listener initialization failed", e);
        }
    }

    private PacketListenerManager() {}

    public static void register(int opcode, PacketListener listener) {
        repository.register(opcode, listener);
    }

    public static PacketListener get(int opcode) {
        if (registrationReport == null || !repository.isLocked()) {
            throw new IllegalStateException("Packet listeners were not initialized during server startup");
        }
        return repository.get(opcode);
    }

    public static boolean isInitialized() {
        return registrationReport != null && repository.isLocked();
    }

    /**
     * Gets the packet repository instance.
     * Useful for advanced operations.
     */
    public static PacketRepository getRepository() {
        return repository;
    }

    private static PacketRegistrationReport buildRegistrationReport() {
        List<Integer> missingCriticalOpcodes = new ArrayList<>();
        for (int opcode : PacketRegistrationReport.CRITICAL_OPCODES) {
            if (!repository.has(opcode)) {
                missingCriticalOpcodes.add(opcode);
            }
        }
        return new PacketRegistrationReport(
            repository.getRegisteredCount(),
            missingCriticalOpcodes,
            repository.getDuplicateOverwriteCount()
        );
    }

    private static void validateCriticalOpcodesOrThrow(PacketRegistrationReport report) {
        if (report.getHasMissingCriticalOpcodes()) {
            throw new IllegalStateException("Missing critical packet listeners: " + report.getMissingCriticalOpcodes());
        }
    }
}
