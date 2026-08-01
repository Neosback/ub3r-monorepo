package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.engine.systems.action.UnifiedPolicyDsl

fun SkillObjectClickBinding.objectPolicy() = UnifiedPolicyDsl.toObjectPolicy(preset)
fun SkillItemOnObjectBinding.objectPolicy() = UnifiedPolicyDsl.toObjectPolicy(preset)
fun SkillMagicOnObjectBinding.objectPolicy() = UnifiedPolicyDsl.toObjectPolicy(preset)
