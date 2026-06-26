package net.dodian.uber.game.engine.systems.net

import net.dodian.uber.game.model.entity.player.Client

/**
 * Thin Kotlin façade for connection-lifecycle packet side-effects that must not
 * live inside the Netty listener layer.
 *
 */
object PacketConnectionService {

    
    @JvmStatic
    fun handleKeepAlive(client: Client) {
        client.resetTimeOutCounter()
    }

    
    @JvmStatic
    fun handleFocusChange(client: Client, focused: Boolean) {
        client.setWindowFocused(focused)
    }

    /**
     * Marks the player as fully loaded into the world (opcodes 121, 210).
     *
     * @param loadCustomObjects true when opcode 121 (initial region load) is received.
     */
    @JvmStatic
    fun handleRegionChange(client: Client, loadCustomObjects: Boolean) {
        if (!client.pLoaded) {
            client.pLoaded = true
        }
        if (!client.IsPMLoaded) {
            client.refreshFriends()
            client.IsPMLoaded = true
        }
        if (loadCustomObjects) {
            client.customObjects()
        }
    }

    /**
     * Does **not** notify friends; the listener performs the friend-list walk
     * which is pure read-only traversal and does not violate boundary rules.
     */
    @JvmStatic
    fun setPrivateChatMode(client: Client, mode: Int) {
        client.Privatechat = mode
    }

}
