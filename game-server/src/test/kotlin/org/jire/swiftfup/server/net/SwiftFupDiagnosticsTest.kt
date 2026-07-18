package org.jire.swiftfup.server.net

import io.netty.channel.embedded.EmbeddedChannel
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.net.codec.FileRequestHandler
import org.jire.swiftfup.server.net.codec.HandshakeRequestHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SwiftFupDiagnosticsTest {
    @BeforeEach
    fun reset() = SwiftFupDiagnostics.resetForTests()

    @AfterEach
    fun cleanUp() = SwiftFupDiagnostics.resetForTests()

    @Test
    fun `missing requests are counted and return the existing empty response`() {
        val channel = EmbeddedChannel(FileRequestHandler(FileResponses()))
        val missing = FilePair(4, 12345)

        assertFalse(channel.writeInbound(missing))
        val response = channel.readOutbound<io.netty.buffer.ByteBuf>()
        assertEquals(missing.bitpack, response.readUnsignedMedium())
        assertEquals(0, response.readInt())
        response.release()

        val snapshot = SwiftFupDiagnostics.snapshot()
        assertEquals(1L, snapshot["requests"])
        assertEquals(1L, snapshot["missingArchives"])
        channel.finishAndReleaseAll()
    }

    @Test
    fun `version mismatch is diagnosed and closes only the cache channel`() {
        val channel = EmbeddedChannel(HandshakeRequestHandler(3, FileResponses()))

        assertFalse(channel.writeInbound(HandshakeRequest(2)))
        val response = channel.readOutbound<io.netty.buffer.ByteBuf>()
        assertTrue(response.isReadable)
        response.release()
        assertFalse(channel.isActive)
        assertEquals(1L, SwiftFupDiagnostics.snapshot()["versionMismatches"])
        channel.finishAndReleaseAll()
    }
}
