package net.dodian.game.event.bootstrap

import net.dodian.game.content.ContentModuleIndex

object CoreEventBusBootstrap {
    @JvmStatic
    fun bootstrap() {
        ContentModuleIndex.eventBootstraps.forEach { bootstrap -> bootstrap() }
    }
}
