package net.dodian.uber.game.content.platform

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

object ModuleConfigRegistry {
    private val logger = LoggerFactory.getLogger(ModuleConfigRegistry::class.java)
    private val bootstrapped = AtomicBoolean(false)
    private val configs = LinkedHashMap<String, PluginModuleConfig>()
    private val metadataByKey = LinkedHashMap<String, PluginModuleMetadata>()

    @JvmStatic
    fun bootstrap(modules: List<PluginModuleMetadata>) {
        if (bootstrapped.get()) {
            return
        }
        synchronized(this) {
            if (bootstrapped.get()) {
                return
            }
            configs.clear()
            metadataByKey.clear()
            for (module in modules) {
                metadataByKey[module.moduleKey] = module
                val config = ContentDataLoader.loadOptional<PluginModuleConfig>(module.configPath) ?: PluginModuleConfig()
                configs[module.moduleKey] = config
            }
            bootstrapped.set(true)
            logger.info("Loaded {} plugin module config entries.", configs.size)
        }
    }

    @JvmStatic
    fun get(moduleKey: String): PluginModuleConfig = configs[moduleKey] ?: PluginModuleConfig()

    @JvmStatic
    fun all(): Map<String, PluginModuleConfig> = configs.toMap()

    @JvmStatic
    fun metadata(): Map<String, PluginModuleMetadata> = metadataByKey.toMap()

    @JvmStatic
    fun resetForTests() {
        bootstrapped.set(false)
        configs.clear()
        metadataByKey.clear()
    }
}
