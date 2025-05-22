package net.dodian.uber.game.network.message;

public class LoginResponseMessage {

    private final int responseCode;
    private final int playerRights;
    private final int flaggedStatus; // Typically 0 for not flagged, 1 for flagged

    /**
     * Constructor for successful login responses.
     *
     * @param responseCode  The response code (e.g., 2 for success).
     * @param playerRights  The player's rights/rank (e.g., 0 for normal, 1 for mod, 2 for admin).
     * @param flaggedStatus The player's flagged status (e.g., 0 for not flagged).
     */
    public LoginResponseMessage(int responseCode, int playerRights, int flaggedStatus) {
        this.responseCode = responseCode;
        this.playerRights = playerRights;
        this.flaggedStatus = flaggedStatus;
    }

    /**
     * Constructor for failure or other non-success login responses.
     * Player rights and flagged status are set to 0 as they are not relevant.
     *
     * @param responseCode The response code (e.g., 3 for invalid username/password, etc.).
     */
    public LoginResponseMessage(int responseCode) {
        this.responseCode = responseCode;
        this.playerRights = 0;  // Not relevant for non-success codes
        this.flaggedStatus = 0; // Not relevant for non-success codes
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getPlayerRights() {
        return playerRights;
    }

    public int getFlaggedStatus() {
        return flaggedStatus;
    }

    @Override
    public String toString() {
        return "LoginResponseMessage{" +
                "responseCode=" + responseCode +
                ", playerRights=" + playerRights +
                ", flaggedStatus=" + flaggedStatus +
                '}';
    }
}
