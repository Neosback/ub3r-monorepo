package net.dodian.uber.game.engine.systems.skills

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.skills.api.SkillMultiConfig

/** Maps module-declared presentation keys to established 317 interfaces. */
object SpecializedSkillMultiRegistry {
    private val renderers = linkedMapOf<String, (Client, SkillMultiConfig) -> Unit>()

    fun register(key: String, renderer: (Client, SkillMultiConfig) -> Unit) {
        require(key.isNotBlank())
        require(renderers.putIfAbsent(key, renderer) == null) { "Duplicate specialized skill presentation: $key" }
    }

    fun render(client: Client, config: SkillMultiConfig): Boolean {
        val renderer = config.presentationKey?.let(renderers::get) ?: return false
        renderer(client, config)
        return true
    }
}
