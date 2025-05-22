package net.dodian.uber.game.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NettyServerTest {

    private int findRandomAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { // 0 means allocate a random available port
            return socket.getLocalPort();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS) // Add a timeout to prevent test hanging indefinitely
    void testServerStartAndStop() throws IOException {
        int port = findRandomAvailablePort();
        NettyServer server = new NettyServer(port);

        // Test server starting
        assertDoesNotThrow(() -> {
            server.start();
        }, "Server should start without throwing an exception.");

        // Test server stopping
        assertDoesNotThrow(() -> {
            server.stop();
        }, "Server should stop without throwing an exception.");
    }

    // TODO: Add test for channel initialization if feasible without too much complexity.
    // This might involve:
    // 1. Starting the server.
    // 2. Creating a simple Netty client (Bootstrap).
    // 3. Connecting the client to the server.
    // 4. Using an EmbeddedChannel or a custom handler in the client to verify
    //    that the server-side pipeline (LoginDecoder, Encoder) is set up for the new channel.
    // 5. Shutting down client and server.
    // Example:
    // @Test
    // void testChannelInitialization() throws Exception {
    //     int port = findRandomAvailablePort();
    //     NettyServer server = new NettyServer(port);
    //     server.start();
    //
    //     EventLoopGroup clientGroup = new NioEventLoopGroup(1);
    //     try {
    //         Bootstrap clientBootstrap = new Bootstrap();
    //         clientBootstrap.group(clientGroup)
    //                 .channel(NioSocketChannel.class)
    //                 .handler(new ChannelInitializer<SocketChannel>() {
    //                     @Override
    //                     protected void initChannel(SocketChannel ch) throws Exception {
    //                         // Client pipeline can be simple, or can have handlers to check server pipeline
    //                         // For instance, if the server sends an initial message upon connection,
    //                         // the client can have a handler to verify that.
    //                         // Or, more advanced, use a custom client handler that sends a probe message
    //                         // and expects a certain response if server pipeline is correct.
    //                     }
    //                 });
    //
    //         ChannelFuture clientFuture = clientBootstrap.connect("localhost", port).sync();
    //         assertTrue(clientFuture.isSuccess(), "Client should connect successfully.");
    //
    //         // How to verify server's pipeline from client?
    //         // - One way is to send a specific message that only the LoginDecoder would understand.
    //         // - Another is if the server-side ChannelInitializer adds a specific handler that sends a message back
    //         //   upon connection, which the client can then verify.
    //         // - For now, successful connection implies the server accepted it and the basic ChannelInitializer ran.
    //         //   The actual presence of LoginDecoder/Encoder in pipeline is harder to assert from client
    //         //   without more specific protocol behavior.
    //
    //         clientFuture.channel().close().sync();
    //     } finally {
    //         server.stop();
    //         clientGroup.shutdownGracefully().syncUninterruptibly();
    //     }
    // }
}
