package net.dodian.uber.game.npc.dsl

@JvmInline
value class NpcProfile(val key: String) {
    override fun toString(): String = key
}
