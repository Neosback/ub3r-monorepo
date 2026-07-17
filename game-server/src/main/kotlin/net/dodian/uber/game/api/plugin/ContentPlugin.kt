package net.dodian.uber.game.api.plugin

/**
 * Common opt-in contract for content which owns startup resources in addition
 * to declarative routes. Content remains discovered as Kotlin singletons and
 * is never dynamically loaded at runtime.
 */
interface ContentPlugin : PluginModuleMetadataProvider, ContentModuleManifestProvider {
    /** Called once after all exclusive route registries have validated. */
    fun start() {}

    /** Called once during orderly shutdown; implementations must be idempotent. */
    fun stop() {}
}

/** Owns the lifecycle of opt-in content plugins without making registration mutable. */
object ContentPluginLifecycle {
    private val started = linkedSetOf<ContentPlugin>()
    private val resources = linkedMapOf<ContentPlugin, MutableList<AutoCloseable>>()

    @Synchronized
    fun start(plugins: Collection<ContentPlugin>) {
        plugins.sortedBy { it.contentManifest.id }.forEach { plugin ->
            if (started.add(plugin)) {
                resources[plugin] = mutableListOf()
                plugin.start()
            }
        }
    }

    @Synchronized
    fun stop() {
        started.toList().asReversed().forEach { plugin ->
            resources.remove(plugin)?.asReversed()?.forEach { it.close() }
            plugin.stop()
        }
        started.clear()
    }

    internal fun resetForTests() = stop()

    /** Registers a resource that will be closed with its owning plugin. */
    @Synchronized fun own(plugin: ContentPlugin, resource: AutoCloseable) {
        check(plugin in started) { "Plugin ${plugin.contentManifest.id} is not started" }
        resources.getValue(plugin) += resource
    }
}
