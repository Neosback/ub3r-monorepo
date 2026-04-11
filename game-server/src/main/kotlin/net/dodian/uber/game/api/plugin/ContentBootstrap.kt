package net.dodian.uber.game.api.plugin

interface ContentBootstrap {
    val id: String

    fun bootstrap()
}
