package net.dodian.uber.game.api.plugin

import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Restart-time module feature controls. Set `-Dcontent.disabled=id[,id...]`
 * to keep a module out of every active registry for this server process.
 */
object ContentModuleFeatureState {
    private val disabled = ConcurrentHashMap.newKeySet<String>()

    init {
        System.getProperty("content.disabled", "")
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .forEach(disabled::add)
    }

    fun isEnabled(manifest: ContentModuleManifest): Boolean =
        manifest.featureFlag == ContentModuleManifest.ALWAYS_ENABLED ||
            (manifest.id !in disabled && manifest.featureFlag !in disabled)

    fun disabledModuleIds(): List<String> = disabled.sorted()
    internal fun setDisabledForTests(ids: Set<String>) { disabled.clear(); disabled.addAll(ids) }
}

data class ContentPlatformSnapshot(
    val modules: List<ContentModuleManifest>,
    val enabledModuleIds: Set<String>,
    val fingerprint: String,
) {
    val enabledCount get() = enabledModuleIds.size
    val disabledCount get() = modules.size - enabledModuleIds.size
    fun module(id: String): ContentModuleManifest? = modules.firstOrNull { it.id == id }
}

/** Immutable, deterministic module inventory used by readiness and staff diagnostics. */
object ContentPlatformCatalog {
    @Volatile private var current = ContentPlatformSnapshot(emptyList(), emptySet(), fingerprint(emptyList()))

    fun publish(manifests: Collection<ContentModuleManifest>) {
        val sorted = manifests.sortedBy { it.id }
        require(sorted.map { it.id }.distinct().size == sorted.size) { "Duplicate content module manifest id" }
        current = ContentPlatformSnapshot(
            modules = sorted,
            enabledModuleIds = sorted.filter(ContentModuleFeatureState::isEnabled).mapTo(linkedSetOf()) { it.id },
            fingerprint = fingerprint(sorted),
        )
    }

    fun snapshot(): ContentPlatformSnapshot = current

    private fun fingerprint(manifests: Collection<ContentModuleManifest>): String {
        val canonical = manifests.sortedBy { it.id }.joinToString("\n") {
            listOf(it.id, it.owner, it.version, it.featureFlag, it.maturity, it.declaredRouteKeys.sorted().joinToString(",")).joinToString("|")
        }
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
