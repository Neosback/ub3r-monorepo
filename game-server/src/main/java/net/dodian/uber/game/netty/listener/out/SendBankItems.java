package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendBankItems implements OutgoingPacket {
    private static final Logger logger = LoggerFactory.getLogger(SendBankItems.class);
    private static final int[] TRACE_ITEM_IDS = {1157, 526, 995, 379, 1193, 1007, 1069, 1731, 1083, 1119};
    private static final int[] TRACE_ITEM_AMOUNTS = {114, 1000, 199713, 279, 108, 152, 99, 35, 94, 91};

    private final List<Integer> itemIds;
    private final List<Integer> amounts;
    private final int interfaceId;
    private final int[] tabAmounts;

    
    public SendBankItems(List<Integer> itemIds, List<Integer> amounts, int interfaceId, int[] tabAmounts) {
        this.itemIds = new ArrayList<>(itemIds);
        this.amounts = new ArrayList<>(amounts);
        this.interfaceId = interfaceId;
        this.tabAmounts = tabAmounts != null ? tabAmounts.clone() : null;
        if (matchesTraceSubset(this.itemIds, this.amounts, interfaceId)) {
            logger.info("SendBankItems: for npc {} , {} , {}", this.itemIds, this.amounts, interfaceId);
        }
    }

    
    public SendBankItems(List<Integer> itemIds, List<Integer> amounts, int interfaceId) {
        this(itemIds, amounts, interfaceId, null);
    }

    
    public SendBankItems(List<Integer> itemIds, List<Integer> amounts) {
        // Default to first bank tab container (50300) for mystic client's bank tabs
        this(itemIds, amounts, 50300);
    }

    @Override
    public void send(Client client) {
        if (itemIds.size() != amounts.size()) {
            throw new IllegalArgumentException("Item IDs and amounts lists must be of equal size");
        }

        int[] ids = new int[itemIds.size()];
        int[] stacks = new int[amounts.size()];
        for (int i = 0; i < itemIds.size(); i++) {
            ids[i] = itemIds.get(i);
            stacks[i] = amounts.get(i);
        }
        ByteMessage message = TarnishItemContainerEncoder.fullPreservingZeroAmounts(interfaceId, ids, stacks, tabAmounts);

        ItemContainerTrace.log(client, "SendBankItems", interfaceId, itemIds.size(), summarizePreview());
        client.send(message);
    }

    private String summarizePreview() {
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < itemIds.size() && preview.length() < 120; i++) {
            if (preview.length() > 0) {
                preview.append(", ");
            }
            preview.append(itemIds.get(i)).append('x').append(amounts.get(i));
        }
        return preview.toString();
    }

    private static boolean matchesTraceSubset(List<Integer> itemIds, List<Integer> amounts, int interfaceId) {
        if (interfaceId != 50300) {
            return false;
        }
        if (itemIds.size() != amounts.size()) {
            return false;
        }
        for (int i = 0; i < TRACE_ITEM_IDS.length; i++) {
            int targetId = TRACE_ITEM_IDS[i];
            int targetAmount = TRACE_ITEM_AMOUNTS[i];
            boolean found = false;
            for (int j = 0; j < itemIds.size(); j++) {
                if (itemIds.get(j) == targetId && amounts.get(j) == targetAmount) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
