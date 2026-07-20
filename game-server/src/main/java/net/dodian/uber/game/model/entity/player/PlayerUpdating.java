package net.dodian.uber.game.model.entity.player;


import net.dodian.uber.game.Server;
import net.dodian.uber.game.model.entity.UpdateFlag;
import net.dodian.uber.game.model.entity.Entity;
import net.dodian.uber.game.model.entity.EntityUpdating;
import net.dodian.uber.game.model.item.Equipment;
import net.dodian.uber.game.item.TarnishEquipmentAppearanceType;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.engine.sync.SynchronizationContext;
import net.dodian.uber.game.engine.sync.player.PlayerVisibilityRules;
import net.dodian.uber.game.engine.sync.scratch.ThreadLocalSyncScratch;
import net.dodian.uber.game.engine.systems.interaction.StaticObjectOverrides;
import net.dodian.utilities.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author blakeman8192
 * @author lare96 <<a href="http://github.com/lare96">...</a>>
 * @author Dashboard
 */
public class PlayerUpdating extends EntityUpdating<Player> {

    private static final Logger logger = LoggerFactory.getLogger(PlayerUpdating.class);
    private static final byte NO_TARNISH_BOUNTY_ICON = -1;

    private static final PlayerUpdating instance = new PlayerUpdating();
    private static final PlayerUpdateBlockSet BLOCK_SET = new PlayerUpdateBlockSet();
    private static final java.util.Map<Integer, String> lastWarnedAppearanceHash = new java.util.concurrent.ConcurrentHashMap<>();
    /** Local-list entry type marking a slot as removed from the viewer's local set. */
    private static final int LOCAL_REMOVE_TYPE = 3;
    /** Sentinel ending the local-list additions loop (client: {@code bitPosition + 10 < packetSize*8}). */
    public static final int LOCAL_LIST_TERMINATOR = 2047;

    enum UpdatePhase {
        UPDATE_SELF,
        UPDATE_LOCAL,
        ADD_LOCAL
    }

    public static PlayerUpdating getInstance() {
        return instance;
    }

    public void updateLocalPlayerMovement(Player player, ByteMessage stream, boolean localPlayerUpdateRequired) {
        stream.startBitAccess();
        if (player.didTeleport()) {
            stream.putBits(1, 1);
            stream.putBits(2, 3); // updateType
            stream.putBits(2, player.getPosition().getZ());
            stream.putBits(1, player.didTeleport() ? 1 : 0);
            stream.putBits(1, localPlayerUpdateRequired ? 1 : 0);
            stream.putBits(7, player.getCurrentY());
            stream.putBits(7, player.getCurrentX());
            return;
        }
        if (player.getPrimaryDirection() == -1) {
            if (localPlayerUpdateRequired) {
                stream.putBits(1, 1);
                stream.putBits(2, 0);
            } else {
                stream.putBits(1, 0);
            }
        } else
        if (player.getSecondaryDirection() == -1) {
            int primaryDirection = translateDirectionToClient(player.getPrimaryDirection(), player.getPlayerName(), "self-primary");
            if (primaryDirection == -1) {
                stream.putBits(1, localPlayerUpdateRequired ? 1 : 0);
                if (localPlayerUpdateRequired) {
                    stream.putBits(2, 0);
                }
                return;
            }
            stream.putBits(1, 1);
            stream.putBits(2, 1);
            stream.putBits(3, primaryDirection);
            stream.putBits(1, localPlayerUpdateRequired ? 1 : 0);
        } else {
            int primaryDirection = translateDirectionToClient(player.getPrimaryDirection(), player.getPlayerName(), "self-primary");
            int secondaryDirection = translateDirectionToClient(player.getSecondaryDirection(), player.getPlayerName(), "self-secondary");
            if (primaryDirection == -1) {
                stream.putBits(1, localPlayerUpdateRequired ? 1 : 0);
                if (localPlayerUpdateRequired) {
                    stream.putBits(2, 0);
                }
                return;
            }
            stream.putBits(1, 1);
            stream.putBits(2, secondaryDirection == -1 ? 1 : 2);
            stream.putBits(3, primaryDirection);
            if (secondaryDirection != -1) {
                stream.putBits(3, secondaryDirection);
            }
            stream.putBits(1, localPlayerUpdateRequired ? 1 : 0);
        }
    }

    private static int translateDirectionToClient(int direction, String playerName, String phase) {
        if (direction < 0 || direction >= Utils.xlateDirectionToClient.length) {
            logger.warn("Invalid player direction {} for {} during {}", direction, playerName, phase);
            return -1;
        }
        return Utils.xlateDirectionToClient[direction];
    }

    public void writeLocalRemoval(ByteMessage stream) {
        stream.putBits(1, 1);
        stream.putBits(2, LOCAL_REMOVE_TYPE);
    }

    /** Writes the active client's add-local wire shape without mutating viewer state. */
    public void writeStagedLocalAdd(Player viewer, Player other, ByteMessage stream, ByteMessage updateBlock) {
        writeStagedLocalAdd(viewer, other, stream, updateBlock, null);
    }

    /** Writes add-local bits and reuses the cycle's immutable appearance block when available. */
    public void writeStagedLocalAdd(Player viewer, Player other, ByteMessage stream, ByteMessage updateBlock,
                                    byte[] sharedBlock) {
        int id = other.getSlot();
        stream.putBits(11, id);
        stream.putBits(1, 1);
        if (sharedBlock != null) {
            updateBlock.putBytes(sharedBlock);
            SynchronizationContext.recordPlayerBlockCacheHit(true);
        } else {
            appendAddLocalBlockUpdate(other, updateBlock);
            SynchronizationContext.recordPlayerBlockCacheHit(false);
        }
        stream.putBits(1, 1);
        int delta = other.getPosition().getY() - viewer.getPosition().getY();
        if (delta < 0) delta += 32;
        stream.putBits(5, delta);
        delta = other.getPosition().getX() - viewer.getPosition().getX();
        if (delta < 0) delta += 32;
        stream.putBits(5, delta);
    }

    /**
     * Add-local for a subject whose current appearance the viewer's client already caches
     * (Apollo-style appearance ticket hit): the ~60-byte appearance block is skipped entirely.
     * The client re-applies its cached appearance for this index on add
     * (playerSynchronizationBuffers), so only the subject's other pending flags — if any — are
     * sent, using the ordinary UPDATE_LOCAL block layout.
     */
    public void writeStagedLocalAddWithoutAppearance(Player viewer, Player other, ByteMessage stream,
                                                     ByteMessage updateBlock) {
        writeStagedLocalAddWithoutAppearance(viewer, other, stream, updateBlock, null);
    }

    /**
     * Add-with-position for a subject whose appearance this viewer has already seen. The block
     * payload is UPDATE_LOCAL — identical bytes to the shared per-tick cache — so callers pass
     * {@code sharedUpdateBlock} when available and only fall back to a per-viewer re-encode on
     * a cache miss (mirrors rsprot's observer-independent block sharing).
     */
    public void writeStagedLocalAddWithoutAppearance(Player viewer, Player other, ByteMessage stream,
                                                     ByteMessage updateBlock, byte[] sharedUpdateBlock) {
        stream.putBits(11, other.getSlot());
        boolean hasBlock = other.getUpdateFlags().isUpdateRequired();
        stream.putBits(1, hasBlock ? 1 : 0);
        if (hasBlock) {
            if (sharedUpdateBlock != null) {
                updateBlock.putBytes(sharedUpdateBlock);
            } else {
                appendBlockUpdate(other, updateBlock, UpdatePhase.UPDATE_LOCAL);
            }
        }
        stream.putBits(1, 1);
        int delta = other.getPosition().getY() - viewer.getPosition().getY();
        if (delta < 0) delta += 32;
        stream.putBits(5, delta);
        delta = other.getPosition().getX() - viewer.getPosition().getX();
        if (delta < 0) delta += 32;
        stream.putBits(5, delta);
    }

    public void appendSelfBlockUpdate(Player player, ByteMessage buf) {
        appendBlockUpdate(player, buf, UpdatePhase.UPDATE_SELF);
    }

    public boolean hasSelfUpdate(Player player) {
        return hasUpdatesForPhase(player, UpdatePhase.UPDATE_SELF);
    }


    @Override
    public void appendBlockUpdate(Player player, ByteMessage buf) {
        appendBlockUpdate(player, buf, UpdatePhase.UPDATE_LOCAL);
    }

    void appendBlockUpdate(Player player, ByteMessage buf, UpdatePhase phase) {
        BLOCK_SET.encode(this, player, buf, phase);
    }

    public void appendAddLocalBlockUpdate(Player player, ByteMessage buf) {
        appendBlockUpdate(player, buf, UpdatePhase.ADD_LOCAL);
    }

    public byte[] buildSharedBlock(Player player, String phaseName) {
        UpdatePhase phase = UpdatePhase.valueOf(phaseName);
        ByteMessage block = withSharedBlock();
        try {
            appendBlockUpdate(player, block, phase);
            return block.toByteArray();
        } finally {
            releaseScratch(block);
        }
    }

    private boolean hasUpdatesForPhase(Player player, UpdatePhase phase) {
        if (phase == UpdatePhase.ADD_LOCAL) {
            return true;
        }
        if (phase == UpdatePhase.UPDATE_SELF) {
            return player.getUpdateFlags().isRequired(UpdateFlag.FORCED_MOVEMENT)
                    || player.getUpdateFlags().isRequired(UpdateFlag.GRAPHICS)
                    || player.getUpdateFlags().isRequired(UpdateFlag.ANIM)
                    || player.getUpdateFlags().isRequired(UpdateFlag.FORCED_CHAT)
                    || player.getUpdateFlags().isRequired(UpdateFlag.FACE_CHARACTER)
                    || player.getUpdateFlags().isRequired(UpdateFlag.APPEARANCE)
                    || player.getUpdateFlags().isRequired(UpdateFlag.FACE_COORDINATE)
                    || player.getUpdateFlags().isRequired(UpdateFlag.HIT)
                    || player.getUpdateFlags().isRequired(UpdateFlag.HIT2);
        }
        return player.getUpdateFlags().isUpdateRequired();
    }

    public void appendGraphic(Player player, ByteMessage buf) {
        buf.putShort(player.getGraphicId(), ByteOrder.LITTLE); // writeWordBigEndian
        buf.putInt(player.getGraphicHeight()); // writeDWord
    }

    @Override
    public void appendAnimationRequest(Player player, ByteMessage buf) {
        buf.putShort(player.getAnimationId(), ByteOrder.LITTLE);
        buf.put(player.getAnimationDelay(), ValueType.NEGATE); // writeByteC = -value, not 128-value
    }

    public static void appendForcedChatText(Player player, ByteMessage buf) {
        String forcedChat = player.getForcedChat();
        // Client does textSpoken.charAt(0) unguarded on this mask; an empty string crashes it.
        buf.putString(forcedChat == null || forcedChat.isEmpty() ? " " : forcedChat);
    }

    public static void appendPlayerChatText(Player player, ByteMessage buf) {
        buf.putShort(((player.getChatTextColor() & 0xFF) << 8) + (player.getChatTextEffects() & 0xFF), ByteOrder.LITTLE); // writeWordBigEndian
        buf.put(player.playerRights);
        int length = Math.min(player.getChatTextSize(), player.getChatText().length);
        buf.put(length, ValueType.NEGATE);
        byte[] encoded = java.util.Arrays.copyOf(player.getChatText(), length);
        buf.putBytesReverse(encoded);
    }

    @Override
    public void appendFaceCharacter(Player player, ByteMessage buf) {
        int faceTarget = player.getFaceTarget();
        if (faceTarget < 0 || faceTarget > 0xFFFF) {
            faceTarget = 0xFFFF;
        }
        buf.putShort(faceTarget, ByteOrder.LITTLE); // writeWordBigEndian
    }

    public static void appendPlayerAppearance(Player player, ByteMessage buf) {
        byte[] appearanceBytes = getInstance().getAppearanceBytes(player);
        buf.put(appearanceBytes.length, ValueType.NEGATE); // writeByteC = -value
        buf.putBytes(appearanceBytes);
    }

    public byte[] getAppearanceBytes(Player player) {
        if (!player.getUpdateFlags().isRequired(UpdateFlag.APPEARANCE)
                && player.isCachedAppearanceValid()) {
            SynchronizationContext.recordPlayerAppearanceCacheHit(true);
            return player.getCachedAppearanceBytes();
        }

        ByteMessage playerProps = withAppearanceScratch();
        try {
            int[] visualLook = TarnishAppearanceValidator.projectLook(player.playerLooks);
            int[] visualEquipment = TarnishAppearanceValidator.projectEquipment(player.getEquipment());
            playerProps.put(visualLook[0]);
            playerProps.put((byte) player.headIcon); // Head icon aka prayer over head
            playerProps.put((byte) player.skullIcon); // Skull icon
            // Tarnish displays bounty icon indexes 0..4. Unsigned 255 means absent.
            playerProps.put(NO_TARNISH_BOUNTY_ICON);
            if (!player.isNpc) {
                if (visualEquipment[Equipment.Slot.HEAD.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.HEAD.getId()]);
                } else {
                    playerProps.put(0);
                }
                if (visualEquipment[Equipment.Slot.CAPE.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.CAPE.getId()]);
                } else {
                    playerProps.put(0);
                }
                if (visualEquipment[Equipment.Slot.NECK.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.NECK.getId()]);
                } else {
                    playerProps.put(0);
                }
                if (visualEquipment[Equipment.Slot.WEAPON.getId()] > 1 && !player.UsingAgility) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.WEAPON.getId()]);
                } else {
                    playerProps.put(0);
                }
                if (visualEquipment[Equipment.Slot.CHEST.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.CHEST.getId()]);
                } else {
                    playerProps.putShort(0x100 + visualLook[3]);
                }
                if (visualEquipment[Equipment.Slot.SHIELD.getId()] > 1 && !player.UsingAgility) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.SHIELD.getId()]);
                } else {
                    playerProps.put(0);
                }
                TarnishEquipmentAppearanceType chestAppearance = Server.itemManager.getTarnishAppearanceType(
                        visualEquipment[Equipment.Slot.CHEST.getId()]);
                if (chestAppearance.getShowArms()) {
                    playerProps.putShort(0x100 + visualLook[4]);
                } else {
                    playerProps.put(0);
                }
                if (visualEquipment[Equipment.Slot.LEGS.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.LEGS.getId()]);
                } else {
                    playerProps.putShort(0x100 + visualLook[6]);
                }
                TarnishEquipmentAppearanceType headAppearance = Server.itemManager.getTarnishAppearanceType(
                        visualEquipment[Equipment.Slot.HEAD.getId()]);
                if (headAppearance.getShowHead()) {
                    playerProps.putShort(0x100 + visualLook[1]); // head
                } else {
                    playerProps.put(0);
                }
                if (visualEquipment[Equipment.Slot.HANDS.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.HANDS.getId()]);
                } else {
                    playerProps.putShort(0x100 + visualLook[5]);
                }
                if (visualEquipment[Equipment.Slot.FEET.getId()] > 1) {
                    playerProps.putShort(0x200 + visualEquipment[Equipment.Slot.FEET.getId()]);
                } else {
                    playerProps.putShort(0x100 + visualLook[7]);
                }
                if (headAppearance.getShowBeard() && visualLook[0] != 1) {
                    playerProps.putShort(0x100 + visualLook[2]);
                } else {
                    playerProps.put(0); // 0 = nothing on and girl don't have beard
                    // so send 0. -bakatool
                }
            } else {
                playerProps.putShort(-1);
                playerProps.putShort(player.getPlayerNpc());
            }
            // array of 5 bytes defining the colors
            playerProps.put(visualLook[8]); // hair color
            playerProps.put(visualLook[9]); // torso color.
            playerProps.put(visualLook[10]); // leg color
            playerProps.put(visualLook[11]); // feet color
            playerProps.put(visualLook[12]); // skin color (0-9)
            int standAnim = player.getStandAnim();
            int walkAnim = player.getWalkAnim();
            int runAnim = player.getRunAnim();
            int walkTurn = walkAnim;
            int turn180 = walkAnim;
            int turn90CW = walkAnim;
            int turn90CCW = walkAnim;

            if (player.getPlayerNpc() >= 0) {
                net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition npcDef =
                    net.dodian.uber.game.npc.NpcClientMorphService.INSTANCE.definition(player.getPlayerNpc());
                if (npcDef != null) {
                    standAnim = npcDef.getStandingAnimation() > 0 ? npcDef.getStandingAnimation() : 808;
                    walkAnim = npcDef.getWalkingAnimation() > 0 ? npcDef.getWalkingAnimation() : 819;
                    runAnim = walkAnim;
                    walkTurn = npcDef.getClockwiseTurnAnimation() > 0 ? npcDef.getClockwiseTurnAnimation() : walkAnim;
                    turn180 = npcDef.getHalfTurnAnimation() > 0 ? npcDef.getHalfTurnAnimation() : walkAnim;
                    turn90CW = npcDef.getClockwiseTurnAnimation() > 0 ? npcDef.getClockwiseTurnAnimation() : walkAnim;
                    turn90CCW = npcDef.getAnticlockwiseTurnAnimation() > 0 ? npcDef.getAnticlockwiseTurnAnimation() : walkAnim;
                }
            }

            playerProps.putShort(standAnim); // standAnimIndex
            playerProps.putShort(walkTurn); // standTurnAnimIndex, 823 default
            playerProps.putShort(walkAnim); // walkAnimIndex
            playerProps.putShort(turn180); // turn180AnimIndex, 820 default
            playerProps.putShort(turn90CW); // turn90CWAnimIndex, 821 default
            playerProps.putShort(turn90CCW); // turn90CCWAnimIndex, 822 default
            playerProps.putShort(runAnim); // runAnimIndex

            playerProps.putLong(Utils.playerNameToInt64(player.getPlayerName()));
            playerProps.putString(""); // title
            playerProps.putInt(0); // title color
            playerProps.putString(""); // clan channel
            playerProps.putString(""); // clan tag
            playerProps.putString(""); // clan tag color
            playerProps.putLong(Double.doubleToLongBits(player.determineCombatLevel()));
            playerProps.put(player.playerRights);
            playerProps.putShort(0); // non-zero displays skill-%d instead of combat level
            byte[] appearanceBytes = playerProps.toByteArray();
            TarnishAppearanceValidator.Validation validation = TarnishAppearanceValidator.validate(appearanceBytes);
            if (!validation.valid) {
                String signature = validation.reason + ":" + validation.hash;
                if (!signature.equals(lastWarnedAppearanceHash.put(player.getSlot(), signature))) {
                    logger.warn("tarnish_appearance_invalid player={} slot={} reason={} hash={} bytes={}",
                            player.getPlayerName(), player.getSlot(), validation.reason, validation.hash, appearanceBytes.length);
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("tarnish_appearance player={} slot={} hash={} bytes={}",
                        player.getPlayerName(), player.getSlot(), validation.hash, appearanceBytes.length);
            }
            player.cacheAppearanceBytes(appearanceBytes);
            SynchronizationContext.recordPlayerAppearanceCacheHit(false);
            return appearanceBytes;
        } finally {
            releaseScratch(playerProps);
        }
    }

    public ByteMessage withScratchUpdateBlock() {
        SynchronizationContext.recordPlayerScratchReuse();
        return ThreadLocalSyncScratch.playerUpdateBlock();
    }

    private ByteMessage withAppearanceScratch() {
        SynchronizationContext.recordPlayerScratchReuse();
        return ThreadLocalSyncScratch.appearanceBlock();
    }

    private ByteMessage withSharedBlock() {
        SynchronizationContext.recordPlayerScratchReuse();
        return ThreadLocalSyncScratch.sharedBlock();
    }

    private static void releaseScratch(ByteMessage message) {
        // Scratch buffers are reused from thread-local storage.
    }

    public void sendServerUpdateIfNeeded(Player player) {
        if (Server.updateRunning) {
            int seconds = Server.updateSeconds + ((int) (Server.updateStartTime - System.currentTimeMillis()) / 1000);
            ((Client) player).send(new net.dodian.uber.game.netty.listener.out.SystemUpdateTimer(seconds * 50 / 30));
        }
    }

    /** Runs ordered viewer-side effects before pure packet planning/encoding. */
    public void prepareViewerSynchronization(Player player) {
        player.syncChunkMembership();
        if (player.didMapRegionChange()) {
            Client client = (Client) player;
            client.send(new net.dodian.uber.game.netty.listener.out.MapRegionUpdate(player.mapRegionX, player.mapRegionY));
            client.updateGroundItems();
            StaticObjectOverrides.replayTo(client);
        }
    }

    @Override
    public void appendFaceCoordinates(Player player, ByteMessage buf) {
        buf.putShort(player.getFaceCoordinateX(), ByteOrder.LITTLE, ValueType.ADD); // writeWordBigEndianA
        buf.putShort(player.getFaceCoordinateY(), ByteOrder.LITTLE); // writeWordBigEndian
    }

    @Override
    public void appendPrimaryHit(Player player, ByteMessage buf) {
        appendTarnishPlayerHit(buf, player.getDamageDealt(), player.getHitType(), player, ValueType.ADD);
    }

    public void appendPrimaryHit2(Player player, ByteMessage buf) {
        appendTarnishPlayerHit(buf, player.getDamageDealt2(), player.getHitType2(), player, ValueType.SUBTRACT);
    }

    private static void appendTarnishPlayerHit(ByteMessage buf, int damage, Entity.hitType hitType,
                                                Player player, ValueType typeTransform) {
        int maximum = player.getMaxHealth() >= 500 ? 200 : 100;
        int health = Math.max(0, Math.min(maximum,
                player.getCurrentHealth() * maximum / Math.max(1, player.getMaxHealth())));
        buf.put(0); // single hit
        buf.put(Math.max(0, Math.min(255, damage)));
        buf.put(tarnishHitType(damage, hitType), typeTransform);
        buf.put(0); // no separate hit icon in the current combat model
        buf.put(health);
        buf.put(maximum, ValueType.NEGATE);
    }

    private static int tarnishHitType(int damage, Entity.hitType hitType) {
        if (damage == 0) return 0;
        if (hitType == Entity.hitType.BURN) return 4;
        if (hitType == Entity.hitType.CRIT) return 3;
        if (hitType == Entity.hitType.POISON) return 2;
        return 1;
    }

}
