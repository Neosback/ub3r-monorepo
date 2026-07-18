package net.dodian.uber.game.engine.systems.net

data class PacketRegistrationReport(
    val registeredCount: Int,
    val missingCriticalOpcodes: List<Int>,
    val duplicateOverwriteCount: Int,
    val discoveredHandlerCount: Int,
    val opcodeOwners: Map<Int, String>,
    val fingerprint: String,
) {
    val hasMissingCriticalOpcodes: Boolean
        get() = missingCriticalOpcodes.isNotEmpty()

    companion object {
        @JvmField
        val CRITICAL_OPCODES = intArrayOf(150, 17, 35, 70, 72, 132, 155, 192, 228, 234, 252)
    }
}
