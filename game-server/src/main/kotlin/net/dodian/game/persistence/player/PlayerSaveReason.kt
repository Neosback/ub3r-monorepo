package net.dodian.game.persistence.player

enum class PlayerSaveReason {
    PERIODIC,
    PERIODIC_PROGRESS,
    TRADE,
    DUEL,
    LOGOUT,
    DISCONNECT,
    SHUTDOWN,
}
