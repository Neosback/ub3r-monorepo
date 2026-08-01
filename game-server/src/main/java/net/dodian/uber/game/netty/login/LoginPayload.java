package net.dodian.uber.game.netty.login;

import io.netty.buffer.ByteBuf;

/**
 * Simple holder for the login payload bytes once fully received.
 * The {@link LoginPayloadDecoder} collects the payload and wraps it
 * in an instance of this class which is then passed to
 * {@link LoginProcessorHandler} for validation and login logic.
 */
public final class LoginPayload {

    private final ByteBuf payload;
    private final boolean reconnecting;

    public LoginPayload(ByteBuf payload, boolean reconnecting) {
        this.payload = payload;
        this.reconnecting = reconnecting;
    }

    /**
     * The retained payload buffer. Callers must release when done.
     */
    public ByteBuf payload() {
        return payload;
    }

    public boolean reconnecting() {
        return reconnecting;
    }
}
