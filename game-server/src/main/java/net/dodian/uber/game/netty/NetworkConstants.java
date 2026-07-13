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
