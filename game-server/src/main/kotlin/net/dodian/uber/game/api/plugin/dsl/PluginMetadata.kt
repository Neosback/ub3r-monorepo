package net.dodian.uber.game.api.plugin.dsl

/**
 * Metadata receiver for the [plugin] builder DSL.
 *
 * Every plugin should declare a [PluginMetadata] block in a top-level file so the
 * registry can display human-readable information about what each plugin does.
 *
 * Inspired by Luna's `PluginBuilderReceiver`.
 */
class PluginMetadata {
    /** The display name of the plugin. */
    var name: String? = null

    /** A short description of what this plugin does. */
    var description: String? = null

    /** The version of this plugin. */
    var version: String = "1.0.0"

    /** The authors who wrote this plugin. */
    val authors: MutableList<String> = ArrayList(2)

    internal fun validate() {
        requireNotNull(name) { "Plugin metadata must specify a 'name'." }
        requireNotNull(description) { "Plugin metadata must specify a 'description'." }
        if (authors.isEmpty()) authors += "Unspecified"
    }
}

