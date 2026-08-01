package net.dodian.uber.game.engine.config

import java.util.concurrent.atomic.AtomicBoolean

object FeatureStateService {
    @JvmField val trading = AtomicBoolean(true)
    @JvmField val dueling = AtomicBoolean(true)
    @JvmField val pvp = AtomicBoolean(true)
    @JvmField val dropping = AtomicBoolean(true)
    @JvmField val banking = AtomicBoolean(true)
    @JvmField val shopping = AtomicBoolean(true)
    @JvmField val publicChatYell = AtomicBoolean(true)

    fun initialize(settings: Settings) {
        trading.set(settings.features.trading)
        dueling.set(settings.features.dueling)
        pvp.set(settings.features.pvp)
        dropping.set(settings.features.dropping)
        banking.set(settings.features.banking)
        shopping.set(settings.features.shopping)
        publicChatYell.set(settings.features.publicChatYell)
    }
}
