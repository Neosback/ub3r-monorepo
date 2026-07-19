package net.dodian.uber.game.netty;

import java.math.BigInteger;
import net.dodian.uber.game.engine.config.DotEnvKt;

public final class NetworkConstants {

    /**
     * Maximum number of inbound packets a single client is allowed to have
     * processed on the game thread per tick (600ms).
     */
    public static final int PACKET_PROCESS_LIMIT_PER_TICK = 25;

    /**
     * Maximum number of inbound packets a single client is allowed to enqueue
     * within a sliding 600ms window (enforced on the Netty event loop).
     */
    public static final int PACKET_RATE_LIMIT_PER_WINDOW = 200;

    /** Close a client that continues flooding after the first rejected packet. */
    public static final int PACKET_RATE_LIMIT_VIOLATIONS_BEFORE_CLOSE = 3;

    /**
     * Backpressure thresholds for the per-client inbound mailbox (rsprot-style):
     * when pending packets reach the pause threshold, the channel's autoRead is
     * disabled so TCP throttles the client instead of the server dropping packets
     * or disconnecting; reading resumes once the game thread drains the backlog
     * below the resume threshold.
     */
    public static final int INBOUND_READ_PAUSE_THRESHOLD = 150;
    public static final int INBOUND_READ_RESUME_THRESHOLD = 50;

    /**
     * Compatibility accessors for code that historically looked in network
     * constants. RSA remains configured and validated exclusively by DotEnv.
     */
    public static BigInteger getRsaModulus() {
        return DotEnvKt.getRsaModulus();
    }

    public static BigInteger getRsaExponent() {
        return DotEnvKt.getRsaExponent();
    }

    private NetworkConstants() {
        
    }
}
