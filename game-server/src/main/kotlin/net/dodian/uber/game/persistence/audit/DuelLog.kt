package net.dodian.uber.game.persistence.audit

object DuelLog {
    @JvmStatic
    fun recordDuel(player: String, opponent: String, playerStake: String, opponentStake: String, winner: String) {
        ConsoleAuditLog.duel(player, opponent, playerStake, opponentStake, winner)
    }
}