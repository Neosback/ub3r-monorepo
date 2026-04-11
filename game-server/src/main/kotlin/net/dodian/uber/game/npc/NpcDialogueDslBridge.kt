package net.dodian.uber.game.npc

typealias NpcSequentialOptionBuilder = net.dodian.uber.game.content.npcs.NpcSequentialOptionBuilder

internal fun buildSequentialHandler(
    init: NpcSequentialOptionBuilder.() -> Unit,
): NpcClickHandler = net.dodian.uber.game.content.npcs.buildSequentialHandler(init)
