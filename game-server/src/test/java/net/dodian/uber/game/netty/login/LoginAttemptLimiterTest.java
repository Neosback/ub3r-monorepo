package net.dodian.uber.game.netty.login;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginAttemptLimiterTest {
    @Test
    void caps_attempts_per_peer_within_a_window() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        long now = 1_000L;

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire("203.0.113.10", now));
        }
        assertFalse(limiter.tryAcquire("203.0.113.10", now));
        assertTrue(limiter.tryAcquire("203.0.113.11", now));
    }

    @Test
    void failed_attempt_applies_backoff_but_success_clears_it() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        long now = 1_000L;
        assertTrue(limiter.tryAcquire("203.0.113.10", now));
        limiter.recordFailure("203.0.113.10", now);
        assertFalse(limiter.tryAcquire("203.0.113.10", now + 999));
        limiter.recordSuccess("203.0.113.10");
        assertTrue(limiter.tryAcquire("203.0.113.10", now + 999));
    }
}
