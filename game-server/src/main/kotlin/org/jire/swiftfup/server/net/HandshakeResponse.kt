package org.jire.swiftfup.server.net

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.directBuffer

/**
 * @author Jire
 */
enum class HandshakeResponse(val buf: ByteBuf) {

    SUCCESS(
        directBuffer(4, 4)
            .writeByte(0)
            .writeMedium(3) // SwiftFUP Version 3
            .asReadOnly()
    ),

    VERSION_MISMATCH(
        directBuffer(1, 1)
            .writeByte(1)
            .asReadOnly()
    )

    ;

}
