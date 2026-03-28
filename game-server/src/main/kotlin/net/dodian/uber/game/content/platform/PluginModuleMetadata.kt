package net.dodian.uber.game.content.platform

data class PluginModuleMetadata(
    val moduleKey: String,
    val moduleClass: String,
    val moduleType: String,
    val configPath: String,
    val dataNamespace: String,
)

data class PluginModuleConfig(
    val enabled: Boolean = true,
    val xpMultiplier: Double = 1.0,
    val debug: Boolean = false,
    val dataPath: String? = null,
)
