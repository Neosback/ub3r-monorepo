package net.dodian.uber.game.model.entity.player;

import net.dodian.uber.comm.Memory;
import net.dodian.uber.game.Constants;
import net.dodian.uber.game.Server; // Needed for Server.login
import net.dodian.uber.comm.LoginManager; // Might be needed if login logic is further centralized
import net.dodian.utilities.Utils;

import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.io.IOException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.BitSet;
import java.util.logging.Logger;

public class PlayerHandler {

    private static final Logger logger = Logger.getLogger(PlayerHandler.class.getName());
    static final Object SLOT_LOCK = new Object();

    static final BitSet usedSlots = new BitSet(Constants.maxPlayers + 1);

    public static ConcurrentHashMap<Long, Client> playersOnline = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, Integer> allOnline = new ConcurrentHashMap<>();
    public static int cycle = 1;

    public static Player[] players = new Player[Constants.maxPlayers + 1];

    public boolean validClient(int index) {
        Client p = (Client) players[index];
        return p != null && !p.disconnected && p.dbId >= 0;
    }

    public Client getClient(int index) {
        return (Client) players[index];
    }

    public PlayerHandler() {
        for (int i = 1; i <= Constants.maxPlayers; i++) {
            players[i] = null;
        }
    }

    public void newPlayerClient(SocketChannel socketChannel, String connectedFrom) {
        int slot;
        synchronized (SLOT_LOCK) {
            slot = findFreeSlot();
            if (slot == -1 || slot > Constants.maxPlayers) {
                logger.warning("No free slots available for a new player connection.");
                closeSocketChannel(socketChannel);
                return;
            }
        }

        logger.info("Attempting to create new client in slot: " + slot);

        Client newClient = null;

        try {
            socketChannel.configureBlocking(false);
            newClient = new Client(socketChannel, slot);
            newClient.handler = this;
            players[slot] = newClient;
            players[slot].connectedFrom = connectedFrom;
            players[slot].ip = ((InetSocketAddress) socketChannel.getRemoteAddress()).getAddress().hashCode();
            newClient.run(); //TODO thread pool would be better

            // Mark the slot as used only after successful login
            if (newClient.isActive) {
                Player.localId = slot;
                playersOnline.put(Utils.playerNameToLong(newClient.getPlayerName()), newClient);
                logger.info("Player " + newClient.getPlayerName() + " successfully added to slot " + slot);
            } else {
                logger.info("Client created but not active for slot " + slot);
                synchronized (SLOT_LOCK) {
                    usedSlots.clear(slot);  // Free the slot if login wasn't successful
                }
            }

            Memory.getSingleton().process(); // Print memory usage after adding player
        } catch (Exception e) {
            logger.severe("Error processing new client connection: " + e.getMessage());
            e.printStackTrace();  // This will give us more detailed error information
            closeSocketChannel(socketChannel);
            synchronized (SLOT_LOCK) {
                usedSlots.clear(slot);  // Free the slot if an exception occurred
            }
        }
    }

    private int findFreeSlot() {
        synchronized (SLOT_LOCK) {
            for (int i = 1; i <= Constants.maxPlayers; i++) {
                if (!usedSlots.get(i)) {
                    usedSlots.set(i);  // Mark the slot as used immediately
                    return i;
                }
            }
        }
        return -1;
    }

    private void closeSocketChannel(SocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException closeError) {
            logger.warning("Error closing socket channel: " + closeError.getMessage());
        }
    }

    public static int getPlayerCount() {
        return (int) playersOnline.size();
    }

    public static boolean isPlayerOn(String playerName) {
        long playerId = Utils.playerNameToLong(playerName);
        if (playersOnline.containsKey(playerId)) {
            logger.info("Player is already logged in as: " + playerName);
            return true;
        }
        return false;
    }

    public static int getPlayerID(String playerName) {
        long playerId = Utils.playerNameToLong(playerName);
        Client client = playersOnline.get(playerId);
        return client != null ? client.getSlot() : -1;
    }

    public int lastchatid = 1; // PM System

    public void removePlayer(Player plr) {
        Client temp = (Client) plr;
        if (temp != null) {
            temp.destruct();
            logger.info("Finished removing player: '" + temp.getPlayerName() + "'");
            int slot = temp.getSlot();
            players[slot] = null;
            if (temp.isActive && slot >= 1 && slot <= Constants.maxPlayers) {
                synchronized (SLOT_LOCK) {
                    usedSlots.clear(slot); // Mark the slot as available
                }
            }
            playersOnline.remove(Utils.playerNameToLong(temp.getPlayerName()));
        } else {
            logger.warning("Tried to remove a null player!");
        }

        Memory.getSingleton().process(); // Print memory usage after removing player
    }

    public static Player getPlayer(String name) {
        long playerId = Utils.playerNameToLong(name);
        return playersOnline.get(playerId);
    }

    public void initializeNettyPlayer(Client authenticatedClient) {
        if (authenticatedClient == null) {
            logger.warning("Attempted to initialize a null authenticatedClient.");
            return;
        }

        int slot;
        synchronized (SLOT_LOCK) {
            slot = findFreeSlot();
        }

        if (slot == -1) {
            logger.warning("Server is full. Cannot initialize Netty player: " + authenticatedClient.getPlayerName());
            // Consider sending a "server full" message to the client via its Netty channel
            // and then closing the channel if a custom message type exists for this.
            // For now, just log and the client will eventually time out or be closed by NettyLoginHandler.
            if(authenticatedClient.getChannel() != null && authenticatedClient.getChannel().isOpen()) {
                // authenticatedClient.getChannel().writeAndFlush(new SomeServerFullMessage()).addListener(ChannelFutureListener.CLOSE);
                authenticatedClient.getChannel().close();
            }
            return;
        }

        logger.info("Initializing Netty player " + authenticatedClient.getPlayerName() + " in slot " + slot);

        authenticatedClient.setSlot(slot);
        players[slot] = authenticatedClient;
        authenticatedClient.handler = this; // 'this' is the PlayerHandler instance
        authenticatedClient.isActive = true;
        // Player.localId = slot; // This static variable usage is problematic for multiple players.
                              // It should ideally be an instance variable or passed around.
                              // For now, setting it as it was, but this is a known issue.
                              // A better approach would be for client instances to know their own slot ID.

        playersOnline.put(Utils.playerNameToLong(authenticatedClient.getPlayerName()), authenticatedClient);
        allOnline.put(Utils.playerNameToLong(authenticatedClient.getPlayerName()), authenticatedClient.getGameWorldId()); // Assuming getGameWorldId() is set or default

        // Call the original initialize method.
        // Note: This method sends packets using the old Stream-based system.
        // These packets will not reach the Netty client correctly until Client.send() and packet encoding
        // are fully adapted for Netty. This is a known limitation for this step.
        authenticatedClient.initialize();

        logger.info("Netty player " + authenticatedClient.getPlayerName() + " initialized and added to slot " + slot + ". Online: " + getPlayerCount());
        Memory.getSingleton().process();
    }
}