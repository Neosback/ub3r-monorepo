package net.dodian.uber.game.netty.login;

import io.netty.channel.Channel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.dodian.uber.game.model.entity.player.Client;

/** Thread-safe state carried across Netty, login-auth, account-db, and the game thread. */
public final class LoginAttempt {
    public enum Stage { RECEIVED, PREPARING, ACCOUNT_LOADING, FINALIZING, COMPLETE, FAILED }

    private final Channel channel;
    private final String remoteIp;
    private final long expectedServerSeed;
    private final byte[] payload;
    private final boolean reconnecting;
    private final long acceptedAtNanos;
    private final AtomicReference<Stage> stage = new AtomicReference<>(Stage.RECEIVED);
    private final AtomicBoolean tokenReleased = new AtomicBoolean();
    private final AtomicBoolean slotReleased = new AtomicBoolean();
    private final AtomicInteger reservedSlot = new AtomicInteger(-1);

    volatile long longName;
    volatile Client client;

    LoginAttempt(Channel channel, String remoteIp, long expectedServerSeed, byte[] payload, boolean reconnecting) {
        this.channel = channel;
        this.remoteIp = remoteIp;
        this.expectedServerSeed = expectedServerSeed;
        this.payload = payload.clone();
        this.reconnecting = reconnecting;
        this.acceptedAtNanos = System.nanoTime();
    }

    public Channel getChannel() { return channel; }
    public String getRemoteIp() { return remoteIp; }
    public long getExpectedServerSeed() { return expectedServerSeed; }
    byte[] payload() { return payload; }
    public boolean isReconnecting() { return reconnecting; }
    public long getAcceptedAtNanos() { return acceptedAtNanos; }
    public Stage getStage() { return stage.get(); }
    public void setStage(Stage next) { stage.set(next); }
    public int getReservedSlot() { return reservedSlot.get(); }
    public void setReservedSlot(int slot) { reservedSlot.set(slot); }
    boolean markTokenReleased() { return tokenReleased.compareAndSet(false, true); }
    boolean markSlotReleased() { return slotReleased.compareAndSet(false, true); }
}
