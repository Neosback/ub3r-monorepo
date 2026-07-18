package net.dodian.uber.game.netty.login;

import net.dodian.utilities.ISAACCipher;

/** Immutable result of the CPU-bound Tarnish login block preparation. */
public final class ParsedLoginRequest {
    private final String username;
    private final String password;
    private final ISAACCipher inboundCipher;
    private final ISAACCipher outboundCipher;
    private final long parseDurationMs;

    ParsedLoginRequest(
            String username,
            String password,
            ISAACCipher inboundCipher,
            ISAACCipher outboundCipher,
            long parseDurationMs
    ) {
        this.username = username;
        this.password = password;
        this.inboundCipher = inboundCipher;
        this.outboundCipher = outboundCipher;
        this.parseDurationMs = parseDurationMs;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public ISAACCipher getInboundCipher() { return inboundCipher; }
    public ISAACCipher getOutboundCipher() { return outboundCipher; }
    public long getParseDurationMs() { return parseDurationMs; }
}
