package net.dodian.uber.game.events.skilling

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.systems.skills.SkillProgressionMode
import net.dodian.uber.game.skill.runtime.action.ActionStopReason

// ── Plugin-framework level ────────────────────────────────────────────────────


data class SkillActionStartEvent(
    val client: Client,
    val actionName: String,
) : GameEvent


data class SkillActionInterruptEvent(
    val client: Client,
    val actionName: String,
    val reason: ActionStopReason,
) : GameEvent


data class SkillActionCompleteEvent(
    val client: Client,
    val actionName: String,
) : GameEvent


data class SkillProgressAppliedEvent(
    val client: Client,
    val skill: Skill,
    val mode: SkillProgressionMode,
    val oldExperience: Int,
    val newExperience: Int,
    val oldLevel: Int,
    val newLevel: Int,
    val appliedAmount: Int,
) : GameEvent

// ── GatheringTask / action-cycle level ───────────────────────────────────────


data class SkillingActionStartedEvent(
    val client: Client,
    val actionName: String,
) : GameEvent


data class SkillingActionCycleEvent(
    val client: Client,
    val actionName: String,
) : GameEvent


data class SkillingActionSucceededEvent(
    val client: Client,
    val actionName: String,
) : GameEvent


data class SkillingActionStoppedEvent(
    val client: Client,
    val actionName: String,
    val reason: ActionStopReason,
) : GameEvent