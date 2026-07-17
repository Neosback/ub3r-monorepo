package net.dodian.uber.game.api.plugin

data class PluginModuleMetadata(val name: String, val description: String, val version: String = "1.0.0", val owner: String = "unspecified")
interface PluginModuleMetadataProvider { val pluginMetadata: PluginModuleMetadata }

enum class ContentMaturity { LEGACY, BETA, STABLE }
data class ContentModuleManifest(
    val id: String,
    val owner: String,
    val version: String,
    val featureFlag: String = ALWAYS_ENABLED,
    val maturity: ContentMaturity = ContentMaturity.BETA,
    val declaredRouteKeys: Set<String> = emptySet(),
) {
    init {
        require(Regex("[a-z][a-z0-9_.-]{2,127}").matches(id)) { "Invalid content module id '$id'" }
        require(owner.isNotBlank() && version.isNotBlank() && featureFlag.isNotBlank())
    }
    companion object { const val ALWAYS_ENABLED = "always" }
}
interface ContentModuleManifestProvider { val contentManifest: ContentModuleManifest }
