package net.dodian.uber.skills.testkit

import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.api.plugin.skills.routeKeys
import net.dodian.uber.game.model.player.skills.Skill

/** Verifies that a Gradle skill module is backed by an active live-server plugin. */
object LiveSkillModuleFixture {
    fun requirePlugin(moduleId: String, skill: Skill) {
        check(moduleId == "skill.${skill.name.lowercase()}") { "Module id $moduleId does not match $skill" }
        val plugin = ContentModuleIndex.skillPlugins.singleOrNull { it.definition.skill == skill }
            ?: error("No live skill plugin registered for $moduleId")
        check(plugin.definition.routeKeys().isNotEmpty()) { "Live skill plugin $moduleId has no routes" }
    }
}
