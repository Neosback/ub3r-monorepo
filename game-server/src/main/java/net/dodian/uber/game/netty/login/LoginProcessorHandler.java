package net.dodian.uber.game.netty.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import net.dodian.utilities.ISAACCipher;
import net.dodian.utilities.Utils;
import net.dodian.uber.game.Constants;
import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.UpdateFlag;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry;
import net.dodian.uber.game.model.entity.player.PlayerInitializer;
import net.dodian.uber.game.engine.metrics.OperationalTelemetry;
import net.dodian.uber.game.netty.codec.ByteMessageEncoder;
import net.dodian.uber.game.netty.game.GamePacketDecoder;
import net.dodian.uber.game.netty.game.GamePacketHandler;
import net.dodian.uber.game.netty.util.ConnectionLoggingHandler;
import net.dodian.uber.game.engine.event.GameEventBus;
import net.dodian.uber.game.engine.loop.GameThreadIngress;
import net.dodian.uber.game.events.player.PlayerLoginEvent;
import net.dodian.uber.game.persistence.account.AccountPersistenceService;
import net.dodian.uber.game.engine.config.DotEnvKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Processes the full second-stage login payload and, on success, creates the
 * {@link Client} instance then swaps the pipeline to in-game handlers.
 */
public class LoginProcessorHandler extends SimpleChannelInboundHandler<LoginPayload> {

    private static final Logger logger = LoggerFactory.getLogger(LoginProcessorHandler.class);
    private static final AtomicLong LOGIN_SLOT_FAILURES = new AtomicLong();
    private static final AtomicLong LOGIN_LOAD_FAILURES = new AtomicLong();
    private static final AtomicLong LOGIN_CHANNEL_CLOSES_BEFORE_FINALIZE = new AtomicLong();
    private static final AtomicLong LOGIN_INITIALIZER_FAILURES = new AtomicLong();
    private static final ConcurrentHashMap<Long, Object> LOADING_ACCOUNTS = new ConcurrentHashMap<>();
    private static final LoginAttemptLimiter LOGIN_ATTEMPTS = new LoginAttemptLimiter();

    private static final int LOGIN_SUCCESS_CODE = 2;
    private static final int RSA_MAGIC          = 255;
    private static final int CLIENT_VERSION     = 317;
    private static final int RSA_PACKET_ID      = 10;

    private static final AttributeKey<ISAACCipher> IN_CIPHER_KEY  = AttributeKey.valueOf("inCipher");
    private static final AttributeKey<ISAACCipher> OUT_CIPHER_KEY = AttributeKey.valueOf("outCipher");

    private long   clientSessionKey;
    private long   serverSessionKey;
    private String username;
    private String password;

    private int  reservedSlot = -1;
    private long  longName = 0L;
    private boolean loginFinished = false;

    public LoginProcessorHandler() {}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginPayload payloadHolder) {
        ByteBuf in = payloadHolder.payload();
        try {
            String remoteIp = remoteIp(ctx);
            if (!LOGIN_ATTEMPTS.tryAcquire(remoteIp, System.currentTimeMillis())) {
                logger.warn("[Netty] Login throttled from {}", remoteIp);
                sendAndClose(ctx, 13);
                return;
            }
            if (!parseLogin(ctx, in)) {
                return; // parseLogin already handled failure
            }
            processLogin(ctx);
        } finally {
            if (in.refCnt() > 0) in.release();
        }
    }

    /* --------------- Parsing --------------- */
    private boolean parseLogin(ChannelHandlerContext ctx, ByteBuf buf) {
        if (buf.readableBytes() < 3) return false;

        int magic = buf.readUnsignedByte();
        if (magic != RSA_MAGIC) {
            logger.debug("[Netty] Bad RSA magic {}", magic);
            ctx.close();
            return false;
        }
        int version = buf.readUnsignedShort();
        if (version != DotEnvKt.getClientVersion()) {
            logger.debug("[Netty] Unsupported client version {}, expected {}", version, DotEnvKt.getClientVersion());
            sendAndClose(ctx, 6);
            return false;
        }
        if (buf.readableBytes() < 1) return false;
        buf.readByte(); // lowMem flag
        if (buf.readableBytes() < 9 * 4 + 1) return false;
        buf.skipBytes(9 * 4); // CRC keys
        int rsaLength = buf.readUnsignedByte();
        if (buf.readableBytes() < rsaLength) return false;

        byte[] rsaBytes = new byte[rsaLength];
        buf.readBytes(rsaBytes);

        // Decrypt using unsigned modPow on java.math.BigInteger
        java.math.BigInteger encrypted = new java.math.BigInteger(1, rsaBytes);
        java.math.BigInteger decrypted = encrypted.modPow(DotEnvKt.getRsaExponent(), DotEnvKt.getRsaModulus());

        byte[] decryptedBytes = decrypted.toByteArray();

        ByteBuf rsaBuf = ctx.alloc().buffer(decryptedBytes.length);
        try {
            rsaBuf.writeBytes(decryptedBytes);

            // Skip a leading zero if present
            if (rsaBuf.readableBytes() > 0 && rsaBuf.getByte(rsaBuf.readerIndex()) == 0) {
                rsaBuf.readByte();
            }

            if (rsaBuf.readableBytes() < 1) {
                logger.warn("Decrypted RSA block is empty");
                ctx.close();
                return false;
            }

            int rsaMagic = rsaBuf.readUnsignedByte();
            if (rsaMagic != 10) {
                logger.warn("RSA magic check failed: expected 10, got {}", rsaMagic);
                ctx.close();
                return false;
            }

            if (rsaBuf.readableBytes() < 8 + 8 + 1) {
                logger.warn("Decrypted RSA block too short");
                ctx.close();
                return false;
            }

            clientSessionKey = rsaBuf.readLong();
            serverSessionKey = rsaBuf.readLong();

            int secondMagic = rsaBuf.readUnsignedByte();
            if (secondMagic != 10) {
                logger.warn("RSA second magic check failed: expected 10, got {}", secondMagic);
                ctx.close();
                return false;
            }

            username = safeReadString(rsaBuf, 12);
            password = safeReadString(rsaBuf, 20);

            if (username.isEmpty() || password.isEmpty()) {
                logger.warn("Username or password empty in decrypted RSA block");
                ctx.close();
                return false;
            }

            // Check for trailing non-zero data
            while (rsaBuf.isReadable()) {
                if (rsaBuf.readByte() != 0) {
                    logger.warn("RSA block contains trailing non-zero data");
                    ctx.close();
                    return false;
                }
            }

        } finally {
            rsaBuf.release();
        }

        logger.debug("[Netty] Login attempt {} from {}", username, ctx.channel().remoteAddress());
        return true;
    }

    private static String safeReadString(ByteBuf buf, int maxLength) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        while (buf.isReadable()) {
            byte b = buf.readByte();
            if (b == 10) {
                break;
            }
            if (count < maxLength) {
                sb.append((char) b);
                count++;
            }
        }
        return sb.toString().trim();
    }

    /* --------------- Login logic --------------- */
    private void processLogin(ChannelHandlerContext ctx) {
        final long acceptedAtNanos = System.nanoTime();

        longName = Utils.playerNameToLong(Utils.capitalize(username.replace('_', ' ')));
        if (LOADING_ACCOUNTS.putIfAbsent(longName, longName) != null) {
            sendAndClose(ctx, 5); // login in progress or already online
            return;
        }
        if (PlayerRegistry.isPlayerOn(username)) {
            releaseToken();
            sendAndClose(ctx, 5); // already online
            return;
        }

        final long slotReserveStart = System.nanoTime();
        reservedSlot = reserveSlot();
        if (reservedSlot == -1) {
            long failures = LOGIN_SLOT_FAILURES.incrementAndGet();
            logger.warn("Login slot reservation failed for {} failures={}", username, failures);
            releaseToken();
            sendAndClose(ctx, 7); // world full
            return;
        }
        final long slotReserveDurationMs = (System.nanoTime() - slotReserveStart) / 1_000_000L;

        // Configure ISAAC
        int[] seed = new int[]{
                (int) (clientSessionKey >>> 32),
                (int) clientSessionKey,
                (int) (serverSessionKey >>> 32),
                (int) serverSessionKey
        };
        ISAACCipher inCipher = new ISAACCipher(seed);
        for (int i = 0; i < 4; i++) seed[i] += 50;
        ISAACCipher outCipher = new ISAACCipher(seed);
        ctx.channel().attr(IN_CIPHER_KEY).set(inCipher);
        ctx.channel().attr(OUT_CIPHER_KEY).set(outCipher);

        // Instantiate Client
        Client client;
        try {
            client = new Client(ctx.channel(), reservedSlot);
        } catch (Exception ex) {
            logger.error("[Netty] Failed to create Client: {}", ex.getMessage());
            releaseSlot(reservedSlot);
            releaseToken();
            sendAndClose(ctx, 13);
            return;
        }
        client.setPlayerName(Utils.capitalize(username.replace('_', ' ')));
        client.playerPass = password;
        client.longName  = longName;
        try {
            InetSocketAddress isa = (InetSocketAddress) ctx.channel().remoteAddress();
            client.connectedFrom = isa.getAddress().getHostAddress();
        } catch (Exception ignored) {}

        final int slotCopy = reservedSlot;
        AccountPersistenceService.submitLoginLoad(client, username, password, loadResult ->
                ctx.channel().eventLoop().execute(() -> finishLogin(ctx, client, loadResult, slotCopy, acceptedAtNanos, slotReserveDurationMs)));
    }

        /**
     * Completes login after the blocking account load has finished.
     * Runs on the Netty event loop thread.
     */
    private void finishLogin(
            ChannelHandlerContext ctx,
            Client client,
            AccountPersistenceService.LoginLoadResult loadResult,
            int slot,
            long acceptedAtNanos,
            long slotReserveDurationMs
    ) {
        if (loadResult.getCode() != 0) {
            LOGIN_ATTEMPTS.recordFailure(remoteIp(ctx), System.currentTimeMillis());
            long failures = LOGIN_LOAD_FAILURES.incrementAndGet();
            OperationalTelemetry.incrementCounter("login.load.failure", 1L);
            logger.warn(
                    "Login load failed for {} code={} load={}ms pendingRetries={} failures={}",
                    client.getPlayerName(),
                    loadResult.getCode(),
                    loadResult.getDurationMs(),
                    loadResult.getPendingRetries(),
                    failures
            );
            releaseSlot(slot);
            releaseToken();
            sendAndClose(ctx, loadResult.getCode());
            return;
        }

        LOGIN_ATTEMPTS.recordSuccess(remoteIp(ctx));

        client.validLogin = true;
        client.playerRights = (client.playerGroup == 9 || client.playerGroup == 5) ? 1 :
                              ((client.playerGroup == 6 || client.playerGroup == 18 || client.playerGroup == 10) ? 2 : 0);
        client.premium = client.playerRights > 0 || client.premium;

        sendLoginSuccess(ctx, client.playerRights);

        // CRITICAL: Setup game pipeline BEFORE PlayerInitializer sends packets
        if (ctx.pipeline().get(net.dodian.uber.game.netty.login.LoginPayloadDecoder.class) != null) {
            ctx.pipeline().remove(net.dodian.uber.game.netty.login.LoginPayloadDecoder.class);
        }
        if (ctx.pipeline().get(net.dodian.uber.game.netty.login.LoginHandshakeHandler.class) != null) {
            ctx.pipeline().remove(net.dodian.uber.game.netty.login.LoginHandshakeHandler.class);
        }
        // Swap pipeline to game mode
        if (ctx.pipeline().get(ConnectionLoggingHandler.class) != null) {
            ctx.pipeline().remove(ConnectionLoggingHandler.class);
        }
        ctx.pipeline().addLast(new GamePacketDecoder());
        ISAACCipher outCipher = ctx.channel().attr(OUT_CIPHER_KEY).get();
        ctx.pipeline().addLast(new ByteMessageEncoder(outCipher));
        // ctx.pipeline().addLast(new GamePacketEncoder()); // Removed - using pure ByteMessage/Netty
        ctx.pipeline().addLast(new GamePacketHandler(client));
        ctx.pipeline().remove(this);

        // Store reference for disconnect cleanup
        ctx.channel().attr(AttributeKey.valueOf("activeClient")).set(client);

        // Finish registration + initialization on the game thread. This avoids cross-thread mutation
        // of PlayerRegistry players[]/playersOnline and reduces login-related sync spikes.
        final io.netty.channel.Channel channel = ctx.channel();
        final int slotCopy = slot;
        final long finalizerQueuedAtNanos = System.nanoTime();
        boolean finalizerAccepted = GameThreadIngress.submitCritical("login-finalize", () -> {
            net.dodian.uber.game.engine.loop.GameThreadContext.validateGameThread("player-registry.login-register");
            long finalizerStartedAtNanos = System.nanoTime();
            long queueWaitMs = (finalizerStartedAtNanos - finalizerQueuedAtNanos) / 1_000_000L;
            if (!channel.isActive() || client.disconnected) {
                long failures = LOGIN_CHANNEL_CLOSES_BEFORE_FINALIZE.incrementAndGet();
                OperationalTelemetry.incrementCounter("login.finalize.channel_closed", 1L);
                // Channel died before the game thread could register the player; release the reserved slot.
                synchronized (PlayerRegistry.slotLock) {
                    PlayerRegistry.usedSlots.clear(slotCopy);
                    net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players[slotCopy] = null;
                }
                releaseToken();
                logger.warn(
                        "Login channel closed before game-thread finalization for {} queueWait={}ms failures={}",
                        client.getPlayerName(),
                        queueWaitMs,
                        failures
                );
                return;
            }

            Client previous = PlayerRegistry.playersOnline.putIfAbsent(client.longName, client);
            if (previous != null) {
                boolean previousStale = previous.disconnected || !previous.isActive
                        || previous.channel == null || !previous.channel.isActive();
                if (!previousStale) {
                    releaseSlot(slotCopy);
                    releaseToken();
                    logger.warn(
                            "Duplicate login prevented for {} queueWait={}ms — another session already active",
                            client.getPlayerName(),
                            queueWaitMs
                    );
                    channel.close();
                    return;
                }
                // previous is stale; replace it
                PlayerRegistry.playersOnline.remove(client.longName, previous);
                PlayerRegistry.playersOnline.put(client.longName, client);
            }
            net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players[slotCopy] = client;
            releaseToken();

            long initializerDurationMs = 0L;
            try {
                long initializerStartNanos = System.nanoTime();
                PlayerInitializer initializer = new PlayerInitializer();
                initializer.initializeCriticalLoginState(client);
                initializerDurationMs = (System.nanoTime() - initializerStartNanos) / 1_000_000L;
                client.initialized = true;

                client.isActive = true;
                if (client.getUpdateFlags() != null) {
                    client.getUpdateFlags().setRequired(UpdateFlag.APPEARANCE, true);
                }
                client.transport(new Position(client.getPosition().getX(), client.getPosition().getY(), client.getPosition().getZ()));
                // Publish to player synchronization only after hydration, appearance and
                // initial placement are all complete.
                client.setSynchronizationReady(true);
                GameEventBus.post(new PlayerLoginEvent(client));

                final PlayerInitializer postInitializer = initializer;
                GameThreadIngress.submitDeferred("login-post-init", () -> {
                    if (!client.disconnected) {
                        postInitializer.initializeDeferredPostLoginState(client);
                    }
                });
            } catch (Exception ex) {
                long failures = LOGIN_INITIALIZER_FAILURES.incrementAndGet();
                OperationalTelemetry.incrementCounter("login.finalize.failure", 1L);
                logger.warn(
                        "[GameThread] PlayerInitializer error for {} failures={}",
                        client.getPlayerName(),
                        failures,
                        ex
                );
                PlayerRegistry.removePlayer(client);
                channel.close();
            }

        });

        if (!finalizerAccepted) {
            OperationalTelemetry.incrementCounter("login.finalize.rejected", 1L);
            logger.warn("Login finalization queue full for {}; closing session", client.getPlayerName());
            releaseSlot(slotCopy);
            releaseToken();
            channel.close();
            return;
        }

        loginFinished = true;
        logger.info(
                "[Netty] Login finished for {} slot {} (async) load={}ms pendingRetries={}",
                client.getPlayerName(),
                slot,
                loadResult.getDurationMs(),
                loadResult.getPendingRetries()
        );
    }

    private static String remoteIp(ChannelHandlerContext ctx) {
        try {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            return address != null && address.getAddress() != null ? address.getAddress().getHostAddress() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /* Slot helpers */
    private int reserveSlot() {
        synchronized (PlayerRegistry.slotLock) {
            for (int i = 1; i <= Constants.maxPlayers; i++) {
                if (!PlayerRegistry.usedSlots.get(i)) {
                    PlayerRegistry.usedSlots.set(i);
                    return i;
                }
            }
        }
        return -1;
    }

    private void releaseSlot(int slot) {
        if (slot <= 0) return;
        synchronized (PlayerRegistry.slotLock) {
            PlayerRegistry.usedSlots.clear(slot);
            net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players[slot] = null;
        }
    }

    private void sendLoginSuccess(ChannelHandlerContext ctx, int rights) {
        ByteBuf resp = ctx.alloc().buffer(2);
        resp.writeByte(LOGIN_SUCCESS_CODE);
        resp.writeByte(rights);
        ctx.writeAndFlush(resp);
    }

    private void sendAndClose(ChannelHandlerContext ctx, int code) {
        ByteBuf resp = ctx.alloc().buffer(3);
        resp.writeByte(code);
        resp.writeByte(0);
        resp.writeByte(0);
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (!loginFinished) {
            releaseToken();
            if (reservedSlot > 0) {
                releaseSlot(reservedSlot);
            }
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("[Netty] Login processing error for {}", ctx.channel().remoteAddress(), cause);
        if (!loginFinished) {
            releaseToken();
            if (reservedSlot > 0) {
                releaseSlot(reservedSlot);
            }
        }
        ctx.close();
    }

    private void releaseToken() {
        if (longName != 0L) {
            LOADING_ACCOUNTS.remove(longName, longName);
        }
    }
}
