package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.api.plugin.PluginModuleMetadata
import net.dodian.uber.game.api.plugin.PluginModuleMetadataProvider
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.engine.systems.action.UnifiedPolicyDsl

interface SkillPlugin : PluginModuleMetadataProvider {
    val definition: SkillPluginDefinition

    override val pluginMetadata: PluginModuleMetadata
        get() = PluginModuleMetadata(
            name = definition.name,
            description = "Skill plugin for ${definition.skill.name.lowercase()}",
            version = "1.0.0",
            owner = "gameplay",
        )
}

data class SkillPluginDefinition(
    val name: String,
    val skill: Skill,
    val objectBindings: List<SkillObjectClickBinding> = emptyList(),
    val npcBindings: List<SkillNpcClickBinding> = emptyList(),
    val itemOnItemBindings: List<SkillItemOnItemBinding> = emptyList(),
    val itemBindings: List<SkillItemClickBinding> = emptyList(),
    val itemOnObjectBindings: List<SkillItemOnObjectBinding> = emptyList(),
    val magicOnObjectBindings: List<SkillMagicOnObjectBinding> = emptyList(),
    val buttonBindings: List<SkillButtonBinding> = emptyList(),
    val lifecycle: SkillPluginLifecycleHooks = SkillPluginLifecycleHooks(),
)

data class SkillPluginLifecycleHooks(
    val onAttempt: ((SkillPlayer) -> Unit)? = null,
    val onStart: ((SkillPlayer) -> Unit)? = null,
    val onCycle: ((SkillPlayer) -> Unit)? = null,
    val onStop: ((SkillPlayer) -> Unit)? = null,
)

data class SkillObjectClickBinding(
    val preset: PolicyPreset,
    val option: Int,
    val objectIds: IntArray,
    val handler: (SkillObjectInteraction) -> Boolean,
)

data class SkillNpcClickBinding(
    val preset: PolicyPreset,
    val option: Int,
    val npcIds: IntArray,
    val handler: (SkillNpcInteraction) -> Boolean,
)

data class SkillItemOnItemBinding(
    val preset: PolicyPreset,
    val leftItemId: Int,
    val rightItemId: Int,
    val handler: (SkillItemOnItemInteraction) -> Boolean,
)

data class SkillItemClickBinding(
    val preset: PolicyPreset,
    val option: Int,
    val itemIds: IntArray,
    val handler: (SkillItemInteraction) -> Boolean,
)

data class SkillItemOnObjectBinding(
    val preset: PolicyPreset,
    val objectIds: IntArray,
    val itemIds: IntArray,
    val handler: (SkillItemOnObjectInteraction) -> Boolean,
)

data class SkillMagicOnObjectBinding(
    val preset: PolicyPreset,
    val objectIds: IntArray,
    val spellIds: IntArray,
    val handler: (SkillMagicOnObjectInteraction) -> Boolean,
)

data class SkillButtonBinding(
    val preset: PolicyPreset,
    val rawButtonIds: IntArray,
    val requiredInterfaceId: Int = -1,
    val opIndex: Int? = null,
    val handler: (SkillButtonInteraction) -> Boolean,
)

fun SkillObjectClickBinding.objectPolicy() = UnifiedPolicyDsl.toObjectPolicy(preset)

fun SkillItemOnObjectBinding.objectPolicy() = UnifiedPolicyDsl.toObjectPolicy(preset)

fun SkillMagicOnObjectBinding.objectPolicy() = UnifiedPolicyDsl.toObjectPolicy(preset)
