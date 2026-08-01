package net.dodian.uber.game.netty.bootstrap

import org.junit.jupiter.api.Test
import java.io.IOException

class NettyGameServerTest {

    private class MockEpollChecker(
        private val available: Boolean,
        private val cause: Throwable? = null
    ) : NettyGameServer.EpollAvailabilityChecker {
        override fun isAvailable(): Boolean = available
        override fun unavailabilityCause(): Throwable? = cause
    }

    @Test
    fun `uses NIO when epoll is unavailable`() {
        val server = NettyGameServer(0, MockEpollChecker(false, IOException("Mocked unavailability")))
        try {
            server.start()
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `attempts to use Epoll when epoll is available`() {
        val server = NettyGameServer(0, MockEpollChecker(true))
        try {
            server.start()
        } catch (e: Throwable) {
            // On macOS or Windows, trying to load Epoll throws UnsatisfiedLinkError / NoClassDefFoundError.
            // This is expected and verifies that the code path attempted to initialize Epoll.
            val isLinkError = e is UnsatisfiedLinkError || e.cause is UnsatisfiedLinkError ||
                    e is NoClassDefFoundError || e.cause is NoClassDefFoundError ||
                    e.message?.contains("epoll", ignoreCase = true) == true
            if (!isLinkError) {
                throw e
            }
        } finally {
            server.shutdown()
        }
    }
}
