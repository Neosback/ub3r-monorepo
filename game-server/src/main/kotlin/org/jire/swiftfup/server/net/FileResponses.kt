package org.jire.swiftfup.server.net

import com.displee.cache.CacheLibrary
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.directBuffer
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.jire.swiftfup.common.GzipCompression
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.FilePair.Companion.writeFilePair
import java.util.zip.CRC32
import org.slf4j.LoggerFactory

/**
 * @author Jire
 */
class FileResponses {

    private val logger = LoggerFactory.getLogger(FileResponses::class.java)

    private val bitpack2Response: Int2ObjectMap<ByteBuf> = Int2ObjectOpenHashMap()

    operator fun get(filePair: FilePair): ByteBuf? = bitpack2Response[filePair.bitpack]

    fun presentAtStartup(filePair: FilePair): Boolean = bitpack2Response.containsKey(filePair.bitpack)

    /** Summary of a [load] pass, so callers (and tests) can assert cache integrity. */
    data class LoadSummary(
        val indices: Int,
        val responses: Int,
        val emptyArchives: Int,
        val unreadableArchives: Int,
        val unreadableSamples: List<FilePair>,
    )

    fun load(cachePath: String, print: Boolean = true): LoadSummary {
        val library = CacheLibrary.create(cachePath)
        val indices = library.validIndices()

        val checksumsBuffer = directBuffer(indices.size * 8)
        checksumsBuffer.writeByte(indices.size)
        var emptyArchives = 0
        var unreadableArchives = 0
        val emptySamples = ArrayList<FilePair>(10)
        val unreadableSamples = ArrayList<FilePair>(10)

        if (print) println("[Building cache responses]")
        for (index in indices) {
            val indexId = index.id

            val archives = index.archives()
            if (print) print("    index $indexId [0]")

            checksumsBuffer
                .writeMedium(archives.size)

            var responsesBuilt = 0
            for (archive in archives) {
                val archiveId = archive.id
                val filePair = FilePair(indexId, archiveId)

                val sector = try {
                    index.readArchiveSector(archiveId)
                } catch (exception: RuntimeException) {
                    unreadableArchives++
                    if (unreadableSamples.size < 10) unreadableSamples += filePair
                    logger.debug("Unable to read SwiftFUP archive {}", filePair, exception)
                    null
                }
                val data = sector?.data
                val dataSize = data?.size ?: 0
                if (dataSize < 1) {
                    emptyArchives++
                    if (emptySamples.size < 10) emptySamples += filePair
                    checksumsBuffer
                        .writeInt(0)
                    continue
                }

                val byteBufSize = FilePair.SIZE_BYTES + 4 + dataSize
                val byteBuf = directBuffer(byteBufSize, byteBufSize)
                    .writeFilePair(filePair)
                    .writeInt(dataSize)
                    .writeBytes(data)

                val crc = CRC32().apply { update(data) }.value.toInt()
                checksumsBuffer.writeInt(crc)

                bitpack2Response[filePair.bitpack] = byteBuf.asReadOnly()
                if (print) {
                    responsesBuilt++

                    var backspaces = "\b"
                    if (responsesBuilt == 1) backspaces += "\b"
                    else repeat((responsesBuilt - 1).toString().length) {
                        backspaces += '\b'
                    }
                    print("$backspaces${responsesBuilt}]")
                }
            }

            if (print) println()
        }
        if (print) println()

        library.close()

        val checksumsBufferArray = ByteArray(checksumsBuffer.readableBytes())
        checksumsBuffer.readBytes(checksumsBufferArray)
        checksumsBuffer.release()

        val compressedChecksumsBufferArray = GzipCompression.compress(checksumsBufferArray)
        val compressedChecksumsBufferArraySize = compressedChecksumsBufferArray.size

        val checksumsResponse = directBuffer(compressedChecksumsBufferArraySize + 4).run {
            writeFilePair(FilePair.checksumsFilePair)
            writeInt(compressedChecksumsBufferArraySize)
            writeBytes(compressedChecksumsBufferArray)
            asReadOnly()
        }
        bitpack2Response[FilePair.checksumsFilePair.bitpack] = checksumsResponse
        logger.info("swiftfup_cache_ready indices={} responses={}", indices.size, bitpack2Response.size)
        if (emptyArchives > 0) {
            logger.debug(
                "swiftfup_sparse_archives emptyArchives={} samples={}",
                emptyArchives,
                emptySamples,
            )
        }
        if (unreadableArchives > 0) {
            logger.warn(
                "swiftfup_cache_unreadable unreadableArchives={} samples={}",
                unreadableArchives,
                unreadableSamples,
            )
        }
        return LoadSummary(indices.size, bitpack2Response.size, emptyArchives, unreadableArchives, unreadableSamples)
    }

}
