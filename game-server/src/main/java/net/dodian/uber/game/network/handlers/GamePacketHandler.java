package net.dodian.uber.game.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.dodian.uber.game.Server;
import net.dodian.uber.game.model.entity.player.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GamePacketHandler extends SimpleChannelInboundHandler<Object> { // Assuming Object for now

    private static final Logger logger = LogManager.getLogger(GamePacketHandler.class);
    private final Client client;

    public GamePacketHandler(Client client) {
        if (client == null) {
            throw new NullPointerException("client");
        }
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Placeholder for handling decoded game packets.
        // In a real implementation, 'msg' would be a specific game packet object.
        // This handler would then:
        // 1. Cast 'msg' to the specific packet type.
        // 2. Call the appropriate game logic based on the packet.
        //    e.g., if (msg instanceof WalkPacket) { client.handleWalk((WalkPacket) msg); }

        logger.debug("GamePacketHandler received message: {} for client: {}",
                     msg.getClass().getSimpleName(), client.getPlayerName());

        // For now, if we receive raw ByteBuf (if GamePacketDecoder doesn't produce specific messages yet):
        // if (msg instanceof ByteBuf) {
        //     ByteBuf buffer = (ByteBuf) msg;
        //     logger.debug("Received ByteBuf of size: {} in GamePacketHandler.", buffer.readableBytes());
        //     // You might attempt to read packet ID here if GamePacketDecoder is very basic.
        //     buffer.release(); // Release if not passed on or consumed.
        // }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Game channel active for client: {} [{}]", client.getPlayerName(), ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Game channel inactive for client: {} [{}]. Initiating cleanup.",
                     client.getPlayerName(), ctx.channel().remoteAddress());

        // Perform player cleanup
        if (!client.disconnected) { // Check if already disconnected to avoid double processing
            client.disconnected = true; // Mark as disconnected
            // Server.playerHandler.removePlayer(client); // This calls client.destruct() internally
                                                       // and also handles slot freeing.
            // client.destruct(); // This is typically called by PlayerHandler.removePlayer
            // If playerHandler is null (e.g. during shutdown), ensure direct cleanup.
            if (Server.playerHandler != null) {
                 Server.playerHandler.removePlayer(client);
            } else {
                client.destruct(); // Fallback if handler is not available
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in GamePacketHandler for client: {} [{}]",
                     client.getPlayerName(), ctx.channel().remoteAddress(), cause);
        ctx.close(); // Close the connection on error
    }
}
