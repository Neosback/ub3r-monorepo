package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.skill.runtime.action.ActionStopReason
import net.dodian.uber.game.skill.runtime.action.CycleSignal
import net.dodian.uber.game.skill.runtime.action.productionAction
import net.dodian.uber.skills.api.SkillRecipe

fun SkillPlayer.startProduction(recipe: SkillRecipe, amount: Int, skill: Skill): SkillActionHandle? {
    if (amount <= 0) return null
    var remaining = amount
    return productionAction("production.${recipe.key}") {
        delay(recipe.delayTicks)
        requirements { level(skill, recipe.requiredLevel); recipe.materials.forEach { item(it.itemId, it.amount, recipe.missingMaterialsMessage) } }
        onCycleSignal {
            if (remaining <= 0) return@onCycleSignal CycleSignal.stop(ActionStopReason.COMPLETED)
            val committed = inventory.transaction { recipe.materials.forEach { remove(it.itemId, it.amount) }; add(recipe.outputItemId, recipe.outputAmount) }
            if (!committed) return@onCycleSignal CycleSignal.stop(ActionStopReason.REQUIREMENT_FAILED)
            if (recipe.animationId >= 0) actions.animate(recipe.animationId)
            remaining--; if (remaining <= 0) CycleSignal.completeSuccess() else CycleSignal.success()
        }
        onSuccess { if (recipe.experience > 0) { skills.gainXp(recipe.experience, skill); actions.triggerRandomEvent(recipe.experience) }; recipe.successMessage?.let(ui::message) }
    }.start(this)
}
