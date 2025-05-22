package net.dodian.uber.game.network.handlers;

import io.netty.channel.*;
import net.dodian.uber.comm.LoginManager;
import net.dodian.uber.game.Server; // For Server.playerHandler
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.network.message.LoginDetailsMessage;
import net.dodian.uber.game.network.message.LoginResponseMessage;
import net.dodian.utilities.IsaacCipher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class NettyLoginHandler extends SimpleChannelInboundHandler<LoginDetailsMessage> {

    private static final Logger logger = LogManager.getLogger(NettyLoginHandler.class);
    private final LoginManager loginManager = new LoginManager(); // Assuming LoginManager can be newed up or is thread-safe.
                                                               // If it holds global state, this might need to be a singleton.

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginDetailsMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        String hostAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

        logger.info("Login attempt from username: {}, IP: {}", username, hostAddress);

        // Simulate Client object creation for LoginManager
        // Note: This Client object is temporary and primarily for the login process.
        // A more permanent Client object will be established by PlayerHandler upon successful login.
        Client tempClient = new Client(ctx.channel(), -1); // -1 indicates a temporary slot
        tempClient.setPlayerName(username);
        tempClient.playerPass = password; // LoginManager expects the raw password here
        tempClient.connectedFrom = hostAddress;
        tempClient.UUID = new String[]{msg.getUidStr(), msg.getMacAddress()};

        // Call LoginManager to authenticate and load game data
        // loginManager.loadgame returns a response code that the client understands.
        // 0 = successful login in LoginManager, maps to 2 for client. Other codes are error codes.
        int responseCode = loginManager.loadgame(tempClient, username, password);

        if (responseCode == 0) { // LoginManager indicates success
            // Player details (rights, flagged status, etc.) are now populated in tempClient by loadgame()
            LoginResponseMessage response = new LoginResponseMessage(2, tempClient.playerRights, tempClient.flagged ? 1 : 0);
            ctx.writeAndFlush(response);

            logger.info("Successful login for user: {}. PlayerRights: {}, Flagged: {}",
                        username, tempClient.playerRights, tempClient.flagged);

            // Store the authenticated (but not yet fully integrated) Client object in channel attributes.
            // This allows PlayerHandler (or a subsequent handler) to pick it up.
            ctx.channel().attr(Client.CLIENT_KEY).set(tempClient);

            // Initialize ISAAC ciphers for the session
            // Client encrypts with original seed array, decrypts with modified (+50) seed array.
            // Server must do the inverse: decrypt with original, encrypt with modified.
            // Correction based on prompt:
            // Server's INBOUND (decrypting client packets) uses MODIFIED client seed.
            // Server's OUTBOUND (encrypting server packets) uses ORIGINAL client seed.

            int[] clientSeedOriginal = msg.getClientSeed(); // This is the 'ai' array from client
            int[] clientSeedForInbound = Arrays.copyOf(clientSeedOriginal, 4);
            for (int i = 0; i < clientSeedForInbound.length; i++) {
                clientSeedForInbound[i] += 50;
            }

            ctx.channel().attr(Client.INBOUND_CIPHER_KEY).set(new IsaacCipher(clientSeedForInbound));
            ctx.channel().attr(Client.OUTBOUND_CIPHER_KEY).set(new IsaacCipher(clientSeedOriginal));
            logger.debug("ISAAC ciphers initialized for {}", username);

            // TODO: The pipeline should be reconfigured now.
            // LoginDecoder and NettyLoginHandler should be removed.
            // GamePacketDecoder and GamePacketEncoder (using ISAAC ciphers) should be added.
            // A GameSessionHandler should be added to connect to PlayerHandler.
            
            // Initialize the player in the game world
            Server.playerHandler.initializeNettyPlayer(tempClient);

            // Reconfigure the pipeline for game packet handling
            ChannelPipeline pipeline = ctx.pipeline();
            
            // Remove login-specific handlers
            if (pipeline.get(LoginDecoder.class) != null) {
                pipeline.remove(LoginDecoder.class);
            } else if (pipeline.get("loginDecoder") != null) {
                 pipeline.remove("loginDecoder");
            }


            // Add game-specific handlers
            // GamePacketDecoder needs the inbound cipher
            IsaacCipher inboundCipher = ctx.channel().attr(Client.INBOUND_CIPHER_KEY).get();
            pipeline.addBefore("encoder", "gamePacketDecoder", new GamePacketDecoder(inboundCipher));
            
            // GamePacketHandler needs the Client instance
            pipeline.addLast("gamePacketHandler", new GamePacketHandler(tempClient));

            // Remove this login handler
            pipeline.remove(this);

            logger.info("Pipeline reconfigured for game session for user: {}", username);

        } else {
            // Login failed, send appropriate response code and close connection
            logger.warn("Failed login for user: {} with response code: {}", username, responseCode);
            LoginResponseMessage response = new LoginResponseMessage(responseCode);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in NettyLoginHandler for " + ctx.channel().remoteAddress(), cause);
        ctx.close(); // Close connection on exception
    }
}
