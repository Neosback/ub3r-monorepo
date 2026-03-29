package net.dodian.game.engine.sync.cache

class RootSynchronizationCache(
    val playerBlocks: SharedPlayerBlockCache = SharedPlayerBlockCache(),
    val npcBlocks: SharedNpcBlockCache = SharedNpcBlockCache(),
)
