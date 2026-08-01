package net.dodian.uber.game.engine.systems.interaction.npcs

object NpcInteractionProfileRegistry {
    private val profiles = linkedSetOf<String>()

    @JvmStatic
    fun normalize(profile: String?): String? = profile?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

    @JvmStatic
    fun register(profile: String?) {
        normalize(profile)?.let { profiles += it }
    }

    @JvmStatic
    fun all(): Set<String> = profiles.toSet()

    internal fun clearForTests() {
        profiles.clear()
    }
}
