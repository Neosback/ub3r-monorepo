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
    
    static {
        try {
            logger.debug("Starting to register packet listeners...");
            Class.forName("net.dodian.uber.game.netty.listener.in.KeepAliveListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.WalkingListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ChangeRegionListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ChangeAppearanceListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ChatListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.UseItemOnPlayerListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.FollowPlayerListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ItemOnGroundItemListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.UseItemOnNpcListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ItemOnItemListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ClickItem2Listener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ClickItemListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ObjectInteractionListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.NpcInteractionListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.MagicOnPlayerListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.TradeListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.TradeRequestListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.SendPrivateMessageListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.MagicOnItemsListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.RemoveItemListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.AttackPlayerListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.WearItemListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.FocusChangeListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.CommandsListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ExamineListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.NpcDropTableListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ClickingButtonsListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.AddFriendListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.AddIgnoreListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.DuelRequestListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.RemoveFriendListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.RemoveIgnoreListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ClickItem3Listener");
            Class.forName("net.dodian.uber.game.netty.listener.in.DialogueListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.ClickingStuffListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.DropItemListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.PickUpGroundItemListener");
            
            Class.forName("net.dodian.uber.game.netty.listener.in.BankAllListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.Bank5Listener");
            Class.forName("net.dodian.uber.game.netty.listener.in.BankX1Listener");
            Class.forName("net.dodian.uber.game.netty.listener.in.BankX2Listener");
            Class.forName("net.dodian.uber.game.netty.listener.in.BankAllButOneListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.BankWithdrawRememberedXListener");
            
            Class.forName("net.dodian.uber.game.netty.listener.in.MagicOnNpcListener");
            
            Class.forName("net.dodian.uber.game.netty.listener.in.Bank10Listener");
            Class.forName("net.dodian.uber.game.netty.listener.in.MouseClicksListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.MoveItemsListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.UpdateChatListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.SyntaxInputListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.InputFieldListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.BankTabCreationListener");
            Class.forName("net.dodian.uber.game.netty.listener.in.DropdownMenuListener");
            
            repository.registerNoOp(77);
            repository.registerNoOp(202);
            repository.registerNoOp(86);
            repository.registerNoOp(229);
            

            PacketRegistrationReport report = buildRegistrationReport();
            validateCriticalOpcodesOrThrow(report);
            logger.info(
                "Packet listener registration summary registered={} duplicate_overwrite_count={} missing_critical={}",
                report.getRegisteredCount(),
                report.getDuplicateOverwriteCount(),
                report.getMissingCriticalOpcodes()
            );

            repository.lock();
            logger.info("All packet listeners registered successfully");
        } catch (Exception e) {
            logger.error("Failed to register packet listeners", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private PacketListenerManager() {}

    public static void register(int opcode, PacketListener listener) {
        repository.register(opcode, listener);
    }

    public static PacketListener get(int opcode) {
        return repository.get(opcode);
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
