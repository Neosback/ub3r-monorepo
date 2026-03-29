package net.dodian.game.systems.interaction

interface InteractionIntent {
    val opcode: Int
    val createdCycle: Long
}
