package net.dodian.uber.game.network.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.dodian.uber.game.network.message.LoginDetailsMessage;
import net.dodian.utilities.ByteBufUtils;
// Removed IsaacCipher import for now as it's not used in this specific decoding stage
// import net.dodian.utilities.IsaacCipher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.List;

public class LoginDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LogManager.getLogger(LoginDecoder.class);

    private enum LoginState {
        CONNECTING,          // Initial state, waiting for connection type and name hash
        SENT_SERVER_SEED,    // Server seed sent, waiting for login block
        LOGIN_ATTEMPT        // Full login block received, processing (or decode complete for this handler)
    }

    private LoginState currentState = LoginState.CONNECTING;
    private long serverSeed; // To store the generated server seed

    // Constants based on client behavior
    private static final int LOGIN_REQUEST_TYPE_STANDARD = 14;
    private static final int LOGIN_REQUEST_TYPE_RECONNECT = 15; // Assuming, not handled yet
    private static final int LOGIN_OPCODE_NEW = 16;
    private static final int LOGIN_OPCODE_RECONNECT = 18;
    private static final int RSA_MAGIC_NUMBER = 255;
    private static final int RSA_OPCODE_10 = 10;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.debug("Decoding with state: {}, readable bytes: {}", currentState, in.readableBytes());

        switch (currentState) {
            case CONNECTING:
                // Client sends:
                // 1. Login Request Type (short, 14 for new login) - 2 bytes
                // 2. Name Hash (short, value is (int) (l >> 16 & 31L) where l is TextClass.longForName(s)) - 2 bytes
                if (in.readableBytes() < 4) { // 2 for request type + 2 for name hash
                    logger.debug("CONNECTING: Not enough bytes for initial handshake (need 4, got {}). Waiting.", in.readableBytes());
                    return;
                }

                int loginRequestType = in.readUnsignedShort(); // Read 2 bytes
                int nameHash = in.readUnsignedShort();         // Read 2 bytes

                logger.info("Received login request type: {}, name hash: {} from {}", loginRequestType, nameHash, ctx.channel().remoteAddress());

                if (loginRequestType == LOGIN_REQUEST_TYPE_STANDARD) {
                    this.serverSeed = new SecureRandom().nextLong();

                    ByteBuf responseBuffer = Unpooled.buffer(17); // 8 zeros + 8 serverSeed + 1 responseCode
                    for (int i = 0; i < 8; i++) {
                        responseBuffer.writeByte(0); // Null/ignored bytes
                    }
                    responseBuffer.writeLong(this.serverSeed);
                    responseBuffer.writeByte(0); // Response code 0: Proceed

                    ctx.writeAndFlush(responseBuffer);
                    logger.info("Sent server seed ({}) to {}. Transitioning to SENT_SERVER_SEED.", this.serverSeed, ctx.channel().remoteAddress());
                    this.currentState = LoginState.SENT_SERVER_SEED;
                } else {
                    logger.warn("Unsupported login request type: {} from {}. Closing connection.", loginRequestType, ctx.channel().remoteAddress());
                    ctx.close();
                }
                break;

            case SENT_SERVER_SEED:
                // Client sends:
                // 1. Login Opcode (byte, 16 for new, 18 for reconnect) - 1 byte
                // 2. Login Block Size (short, size of the rest of the packet) - 2 bytes
                // Total 3 bytes for this header.
                if (in.readableBytes() < 3) {
                    logger.debug("SENT_SERVER_SEED: Not enough bytes for login block header (need 3, got {}). Waiting.", in.readableBytes());
                    return;
                }

                int loginOpcode = in.readUnsignedByte(); // Read 1 byte
                logger.debug("Login opcode: {}", loginOpcode);

                if (loginOpcode != LOGIN_OPCODE_NEW && loginOpcode != LOGIN_OPCODE_RECONNECT) {
                    logger.warn("Invalid login opcode: {} from {}. Closing connection.", loginOpcode, ctx.channel().remoteAddress());
                    ctx.close();
                    return;
                }

                int loginBlockSize = in.readUnsignedShort(); // Read 2 bytes
                logger.debug("Login block size: {}", loginBlockSize);

                if (in.readableBytes() < loginBlockSize) {
                    logger.debug("SENT_SERVER_SEED: Not enough bytes for full login block (need {}, got {}). Waiting.", loginBlockSize, in.readableBytes());
                    // We must reset reader index for the opcode and size as they are part of the block size count.
                    // ByteToMessageDecoder will recall decode with more data.
                    in.readerIndex(in.readerIndex() - 3); // Rewind: 1 for opcode, 2 for size
                    return;
                }

                // Now read the main login block
                // Structure:
                // 3. Magic Number (short, 255) - 2 bytes (client writes with writeWordBigEndian)
                // 4. Client Version (short) - 2 bytes
                // 5. Low Memory Flag (short, 0 or 1) - 2 bytes (client writes with writeWordBigEndian)
                // 6. Archive CRCs (9 * int) - 9 * 4 = 36 bytes
                // 7. RSA Payload Length (short) - 2 bytes
                // 8. RSA Payload (variable, rsaPayloadLength bytes)
                //    a. RSA Opcode (short, 10) - 2 bytes
                //    b. Client Seed (4 * int) - 4 * 4 = 16 bytes
                //    c. UID string
                //    d. MAC Address string
                //    e. Username string
                //    f. Password string

                int magicNumber = in.readUnsignedShort(); // Client uses writeWordBigEndian, but value 255 fits in a byte. Let's assume it's read as short.
                if (magicNumber != RSA_MAGIC_NUMBER) { // Assuming client sends 255 as a short
                    logger.warn("Invalid magic number: {} (expected {}) from {}. Closing connection.", magicNumber, RSA_MAGIC_NUMBER, ctx.channel().remoteAddress());
                    ctx.close();
                    return;
                }

                int clientVersion = in.readUnsignedShort();
                int lowMemoryFlag = in.readUnsignedShort(); // Client uses writeWordBigEndian

                int[] archiveCRCs = new int[9];
                for (int i = 0; i < 9; i++) {
                    archiveCRCs[i] = in.readInt();
                }

                int rsaPayloadLength = in.readUnsignedShort();
                if (in.readableBytes() < rsaPayloadLength) {
                    logger.warn("SENT_SERVER_SEED: Data inconsistency. Expected RSA payload length {} but only {} readable. Closing.", rsaPayloadLength, in.readableBytes());
                    // This state is problematic as part of the overall loginBlockSize was consumed.
                    // To prevent partial reads, we should have checked loginBlockSize against expected fixed parts + rsaPayloadLength earlier.
                    // For now, close. A more robust solution would ensure the initial loginBlockSize check is accurate for all parts.
                    ctx.close();
                    return;
                }

                ByteBuf rsaBlock = in.readBytes(rsaPayloadLength);

                int rsaOpcode = rsaBlock.readUnsignedShort(); // Should be 10
                if (rsaOpcode != RSA_OPCODE_10) {
                    logger.warn("Invalid RSA opcode: {} (expected {}) from {}. Closing connection.", rsaOpcode, RSA_OPCODE_10, ctx.channel().remoteAddress());
                    ctx.close();
                    rsaBlock.release();
                    return;
                }

                int[] clientSeed = new int[4];
                for (int i = 0; i < 4; i++) {
                    clientSeed[i] = rsaBlock.readInt();
                }

                // Assuming ByteBufUtils.readString exists and handles the null-terminated string format
                String uidStr = ByteBufUtils.readString(rsaBlock); // Or other string like "1543456788"
                String macAddress = ByteBufUtils.readString(rsaBlock);
                String username = ByteBufUtils.readString(rsaBlock).trim().toLowerCase();
                String password = ByteBufUtils.readString(rsaBlock);

                rsaBlock.release(); // Release the separately sliced rsaBlock

                LoginDetailsMessage details = new LoginDetailsMessage(username, password, clientSeed, this.serverSeed, clientVersion, archiveCRCs);
                out.add(details);

                logger.info("Successfully decoded login details for user '{}' from {}. Transitioning to LOGIN_ATTEMPT.", username, ctx.channel().remoteAddress());
                this.currentState = LoginState.LOGIN_ATTEMPT; // Or a more final state like DECODING_COMPLETE
                break;

            case LOGIN_ATTEMPT:
                // The decoder's job for login details is done.
                // Further communication (e.g., game packets) would be handled by different decoders/handlers
                // after the pipeline is potentially reconfigured post-login.
                // If any bytes are left, it might be an error or for a subsequent handler.
                if (in.readableBytes() > 0) {
                    logger.warn("LOGIN_ATTEMPT: Unexpected readable bytes ({}) after login decoding complete for {}. Discarding.", in.readableBytes(), ctx.channel().remoteAddress());
                    in.skipBytes(in.readableBytes());
                }
                // It's important that this decoder is removed or replaced in the pipeline after successful login,
                // otherwise it will keep consuming bytes in the LOGIN_ATTEMPT state.
                break;

            default:
                logger.error("Unhandled login state: {} for {}. Closing connection.", currentState, ctx.channel().remoteAddress());
                ctx.close();
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in LoginDecoder for " + ctx.channel().remoteAddress() + " in state " + currentState, cause);
        ctx.close();
    }
}
