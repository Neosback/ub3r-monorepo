package net.dodian.uber.game.api.plugin

import net.dodian.uber.game.api.plugin.ContentBootstrap
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.SkillPluginRegistryEngine
import net.dodian.uber.game.api.plugin.skills.SkillPluginSnapshot

object PluginRegistry : ContentBootstrap {
    override val id: String = "plugins.registry"

    private val skills = SkillPluginRegistryEngine()

    override fun bootstrap() {
        skills.bootstrap(ContentModuleIndex.skillPlugins)
    }

    fun currentSkills(): SkillPluginSnapshot {
        bootstrap()
        return skills.current()
    }

    fun registerSkill(plugin: SkillPlugin) = skills.register(plugin)

    internal fun clearForTests() = skills.clearForTests()

    fun resetForTests() = skills.resetForTests()
}
