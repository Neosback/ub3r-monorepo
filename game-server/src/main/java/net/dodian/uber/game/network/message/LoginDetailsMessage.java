package net.dodian.uber.game.network.message;

import java.util.Arrays;

public class LoginDetailsMessage {

    private final String username;
    private final String password;
    private final int[] clientSeed; // Should be size 4
    private final long serverSeed;
    private final int clientVersion;
    private final int[] archiveCRCs; // Should be size 9
    private final String uidStr;
    private final String macAddress;

    public LoginDetailsMessage(String username, String password, int[] clientSeed, long serverSeed,
                               int clientVersion, int[] archiveCRCs, String uidStr, String macAddress) {
        this.username = username;
        this.password = password;
        this.clientSeed = clientSeed;
        this.serverSeed = serverSeed;
        this.clientVersion = clientVersion;
        this.archiveCRCs = archiveCRCs;
        this.uidStr = uidStr;
        this.macAddress = macAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int[] getClientSeed() {
        return Arrays.copyOf(clientSeed, clientSeed.length); // Defensive copy
    }

    public long getServerSeed() {
        return serverSeed;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public int[] getArchiveCRCs() {
        return Arrays.copyOf(archiveCRCs, archiveCRCs.length); // Defensive copy
    }

    public String getUidStr() {
        return uidStr;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        return "LoginDetailsMessage{" +
                "username='" + username + '\'' +
                ", password='****'" + // Avoid logging password directly
                ", clientSeed=" + Arrays.toString(clientSeed) +
                ", serverSeed=" + serverSeed +
                ", clientVersion=" + clientVersion +
                ", archiveCRCs=" + Arrays.toString(archiveCRCs) +
                ", uidStr='" + uidStr + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
