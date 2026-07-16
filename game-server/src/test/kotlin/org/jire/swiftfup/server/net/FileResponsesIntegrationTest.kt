package org.jire.swiftfup.server.net

import org.jire.swiftfup.server.FilePair
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path

class FileResponsesIntegrationTest {
    @Test
    fun `serves checksum and startup archive responses from the shipped cache`() {
        val responses = FileResponses()
        responses.load(cachePath().toString(), print = false)

        val checksums = requireNotNull(responses[FilePair.checksumsFilePair])
        assertEquals(FilePair.checksumsFilePair.bitpack, checksums.readUnsignedMedium())
        assertTrue(checksums.readInt() > 0)

        val titleArchive = requireNotNull(responses[FilePair(0, 1)])
        assertEquals(FilePair(0, 1).bitpack, titleArchive.readUnsignedMedium())
        assertTrue(titleArchive.readInt() > 0)
    }

    @Test
    fun `version three client handshake receives requested archive over the embedded server`() {
        val responses = FileResponses()
        responses.load(cachePath().toString(), print = false)
        val port = ServerSocket(0).use { it.localPort }
        val server = FileServer(3, responses)

        try {
            server.start(port)
            Socket("127.0.0.1", port).use { socket ->
                socket.soTimeout = 5_000
                socket.getOutputStream().apply {
                    write(medium(3))
                    flush()
                }
                assertArrayEquals(byteArrayOf(0, 0, 0, 3), socket.getInputStream().readNBytes(4))

                socket.getOutputStream().apply {
                    write(medium(FilePair(0, 1).bitpack))
                    flush()
                }
                val header = socket.getInputStream().readNBytes(FilePair.SIZE_BYTES + Int.SIZE_BYTES)
                assertEquals(FilePair(0, 1).bitpack, medium(header, 0))
                val length = int(header, FilePair.SIZE_BYTES)
                assertTrue(length > 0)
                assertEquals(length, socket.getInputStream().readNBytes(length).size)
            }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `cache server closes only the channel that exceeds its request budget`() {
        val responses = FileResponses()
        responses.load(cachePath().toString(), print = false)
        val port = ServerSocket(0).use { it.localPort }
        val server = FileServer(3, responses)

        try {
            server.start(port)
            Socket("127.0.0.1", port).use { client ->
                client.soTimeout = 5_000
                handshake(client)
                requestAndDrain(client, FilePair(0, 1))
            }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `multiple connections from same ip both succeed without protection`() {
        val responses = FileResponses()
        responses.load(cachePath().toString(), print = false)
        val port = ServerSocket(0).use { it.localPort }
        val server = FileServer(3, responses)

        try {
            server.start(port)
            Socket("127.0.0.1", port).use { first ->
                first.soTimeout = 5_000
                handshake(first)
                requestAndDrain(first, FilePair(0, 1))
            }
            Socket("127.0.0.1", port).use { second ->
                second.soTimeout = 5_000
                handshake(second)
                requestAndDrain(second, FilePair(0, 1))
            }
        } finally {
            server.shutdown()
        }
    }

    private fun handshake(socket: Socket) {
        socket.getOutputStream().apply {
            write(medium(3))
            flush()
        }
        assertArrayEquals(byteArrayOf(0, 0, 0, 3), socket.getInputStream().readNBytes(4))
    }

    private fun requestAndDrain(socket: Socket, pair: FilePair) {
        socket.getOutputStream().apply {
            write(medium(pair.bitpack))
            flush()
        }
        val header = socket.getInputStream().readNBytes(FilePair.SIZE_BYTES + Int.SIZE_BYTES)
        assertEquals(pair.bitpack, medium(header, 0))
        val length = int(header, FilePair.SIZE_BYTES)
        assertTrue(length > 0)
        assertEquals(length, socket.getInputStream().readNBytes(length).size)
    }

    private fun cachePath(): Path =
        sequenceOf(Path.of("data/cache"), Path.of("game-server/data/cache"))
            .firstOrNull(Files::isDirectory)
            ?: error("Unable to locate game-server cache")

    private fun medium(value: Int): ByteArray =
        byteArrayOf((value ushr 16).toByte(), (value ushr 8).toByte(), value.toByte())

    private fun medium(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            (bytes[offset + 2].toInt() and 0xFF)

    private fun int(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
}
