package net.dodian.uber.game.engine.sync

/**
 * Selects the player-info encoder. Staged is the beta default, canonical is the
 * correctness fallback, and the older optimizer still requires explicit opt-in.
 */
enum class PlayerSynchronizationMode {
    STAGED,
    CANONICAL,
    OPTIMIZED;

    companion object {
        @JvmStatic
        fun configured(): PlayerSynchronizationMode {
            val configured =
                System.getProperty("player.sync.mode")
                    ?: System.getenv("PLAYER_SYNC_MODE")
                    ?: return STAGED
            return entries.firstOrNull { it.name.equals(configured.trim(), ignoreCase = true) }
                ?: STAGED
        }
    }
}
