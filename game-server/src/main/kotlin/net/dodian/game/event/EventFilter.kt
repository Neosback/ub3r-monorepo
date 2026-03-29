package net.dodian.game.event

fun interface EventFilter<E : GameEvent> {
    fun test(event: E): Boolean
}
