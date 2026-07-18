package net.dodian.uber.game.netty.login

import io.netty.channel.embedded.EmbeddedChannel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoginPreparationServiceTest {
    @Test
    fun `login block preparation executes outside the Netty event loop`() {
        val channel = EmbeddedChannel()
        val attempt = LoginAttempt(channel, "127.0.0.1", 123L, byteArrayOf(1), false)
        val completed = CountDownLatch(1)
        val workerName = AtomicReference<String>()
        val resultRef = AtomicReference<LoginPreparationService.Result>()

        assertTrue(
            LoginPreparationService.submit(attempt) { result ->
                workerName.set(Thread.currentThread().name)
                resultRef.set(result)
                completed.countDown()
            },
        )

        assertTrue(completed.await(5, TimeUnit.SECONDS))
        assertTrue(workerName.get().startsWith(LoginPreparationService.workerThreadPrefix()))
        assertFalse(resultRef.get().isSuccess)
        channel.finishAndReleaseAll()
    }
}
