package net.dodian.uber.game.model.entity.player

import java.util.concurrent.ConcurrentHashMap
import net.dodian.uber.game.engine.lifecycle.DeathTaskState
import net.dodian.uber.game.engine.scheduler.QueueTaskHandle
import net.dodian.uber.game.engine.systems.action.ActiveProductionSelection
import net.dodian.uber.game.engine.systems.action.PendingProductionSelection
import net.dodian.uber.game.engine.systems.action.PlayerActionCancelReason
import net.dodian.uber.game.engine.systems.action.PlayerActionType
import net.dodian.uber.game.engine.systems.combat.CombatCancellationReason
import net.dodian.uber.game.engine.systems.combat.CombatCooldownState
import net.dodian.uber.game.engine.systems.combat.CombatEngagementState
import net.dodian.uber.game.engine.systems.combat.CombatTargetState
import net.dodian.uber.game.engine.systems.interaction.ActiveInteraction
import net.dodian.uber.game.engine.systems.interaction.InteractionAnchorState
import net.dodian.uber.game.engine.systems.interaction.InteractionIntent
import net.dodian.uber.game.engine.tasking.GameTaskSet
import net.dodian.uber.game.skill.cooking.CookingState
import net.dodian.uber.game.skill.crafting.CraftingState
import net.dodian.uber.game.skill.fishing.FishingState
import net.dodian.uber.game.skill.fletching.FletchingState
import net.dodian.uber.game.skill.mining.MiningState
import net.dodian.uber.game.skill.prayer.PrayerOfferingState
import net.dodian.uber.game.skill.runecrafting.RunecraftingState
import net.dodian.uber.game.skill.smithing.ActiveSmithingSelection
import net.dodian.uber.game.skill.smithing.SmeltingSelection
import net.dodian.uber.game.skill.thieving.PyramidPlunderPlayerState
import net.dodian.uber.game.skill.woodcutting.WoodcuttingState
import net.dodian.uber.game.api.plugin.skills.PendingSkillMulti

/**
 * Content-owned, per-player runtime state. This intentionally has no protocol
 * dependency and is the Kotlin home for skill/action/session state formerly
 * embedded in Player's Java interaction holder.
 */
class PlayerContentRuntimeState {
    @Volatile private var pendingInteraction: InteractionIntent? = null
    @Volatile private var activeInteraction: ActiveInteraction? = null
    @Volatile private var interactionEarliestCycle = 0L
    @Volatile private var interactionTaskHandle: QueueTaskHandle? = null
    @Volatile private var farmDebugTaskHandle: QueueTaskHandle? = null
    @Volatile private var miningTaskHandle: QueueTaskHandle? = null
    @Volatile private var woodcuttingTaskHandle: QueueTaskHandle? = null
    @Volatile private var activeActionHandle: QueueTaskHandle? = null
    @Volatile private var activeActionType: PlayerActionType? = null
    @Volatile private var actionStartedCycle = 0L
    @Volatile private var activeActionCancelReason: PlayerActionCancelReason? = null
    @Volatile private var lastActionCancelReason: PlayerActionCancelReason? = null
    @Volatile private var lastActionCancelCycle = 0L
    @Volatile private var combatTargetState: CombatTargetState? = null
    @Volatile private var combatEngagementState: CombatEngagementState? = null
    @Volatile private var combatCooldownState: CombatCooldownState? = null
    @Volatile private var attackStartDedupeState: AttackStartDedupeState? = null
    @Volatile private var combatCancellationReason: CombatCancellationReason? = null
    @Volatile private var combatLogoutLockUntilCycle = 0L
    @Volatile private var lastBlockAnimationCycle = -1L
    @Volatile private var deathTaskState: DeathTaskState? = null
    @Volatile private var activeSmithingSelection: ActiveSmithingSelection? = null
    @Volatile private var smeltingSelection: SmeltingSelection? = null
    @Volatile private var pendingSmeltingBarId = -1
    @Volatile private var pendingProductionSelection: PendingProductionSelection? = null
    @Volatile private var activeProductionSelection: ActiveProductionSelection? = null
    @Volatile private var playerTaskSet: GameTaskSet<*>? = null
    @Volatile private var miningState: MiningState? = null
    @Volatile private var woodcuttingState: WoodcuttingState? = null
    @Volatile private var fletchingState: FletchingState? = null
    @Volatile private var fishingState: FishingState? = null
    @Volatile private var cookingState: CookingState? = null
    @Volatile private var craftingState: CraftingState? = null
    @Volatile private var prayerOfferingState: PrayerOfferingState? = null
    @Volatile private var runecraftingState: RunecraftingState? = null
    @Volatile private var interactionAnchorState: InteractionAnchorState? = null
    @Volatile private var pyramidPlunderState: PyramidPlunderPlayerState? = null
    @Volatile private var movementLockState: MovementLockState? = null
    @Volatile private var agilitySessionState: AgilitySessionState? = null
    @Volatile private var skillingEventState: SkillingEventState? = null
    @Volatile private var playerPotatoState: PlayerPotatoState? = null
    @Volatile private var activeSkillSessionKey: String? = null
    @Volatile private var activeSkillSessionStartedCycle = 0L
    @Volatile private var pendingSkillMulti: PendingSkillMulti? = null
    private val throttleUntilCycles = ConcurrentHashMap<String, Long>()

    fun getPendingInteraction() = pendingInteraction
    fun setPendingInteraction(value: InteractionIntent?) { pendingInteraction = value }
    fun getActiveInteraction() = activeInteraction
    fun setActiveInteraction(value: ActiveInteraction?) { activeInteraction = value }
    fun getInteractionEarliestCycle() = interactionEarliestCycle
    fun setInteractionEarliestCycle(value: Long) { interactionEarliestCycle = value }

    fun getInteractionTaskHandle() = interactionTaskHandle
    fun setInteractionTaskHandle(value: QueueTaskHandle?) { interactionTaskHandle = value }
    fun cancelInteractionTask() { interactionTaskHandle.cancelAndClear { interactionTaskHandle = null } }
    fun getFarmDebugTaskHandle() = farmDebugTaskHandle
    fun setFarmDebugTaskHandle(value: QueueTaskHandle?) { farmDebugTaskHandle = value }
    fun cancelFarmDebugTask() { farmDebugTaskHandle.cancelAndClear { farmDebugTaskHandle = null } }
    fun getMiningTaskHandle() = miningTaskHandle
    fun setMiningTaskHandle(value: QueueTaskHandle?) { miningTaskHandle = value }
    fun cancelMiningTask() { miningTaskHandle.cancelAndClear { miningTaskHandle = null } }
    fun getWoodcuttingTaskHandle() = woodcuttingTaskHandle
    fun setWoodcuttingTaskHandle(value: QueueTaskHandle?) { woodcuttingTaskHandle = value }
    fun cancelWoodcuttingTask() { woodcuttingTaskHandle.cancelAndClear { woodcuttingTaskHandle = null } }

    fun getMiningState() = miningState
    fun setMiningState(value: MiningState?) { miningState = value }
    fun clearMiningState() { miningState = null }
    fun getWoodcuttingState() = woodcuttingState
    fun setWoodcuttingState(value: WoodcuttingState?) { woodcuttingState = value }
    fun clearWoodcuttingState() { woodcuttingState = null }
    fun getFletchingState() = fletchingState
    fun setFletchingState(value: FletchingState?) { fletchingState = value }
    fun clearFletchingState() { fletchingState = null }
    fun getFishingState() = fishingState
    fun setFishingState(value: FishingState?) { fishingState = value }
    fun clearFishingState() { fishingState = null }
    fun getCookingState() = cookingState
    fun setCookingState(value: CookingState?) { cookingState = value }
    fun clearCookingState() { cookingState = null }
    fun getCraftingState() = craftingState
    fun setCraftingState(value: CraftingState?) { craftingState = value }
    fun clearCraftingState() { craftingState = null }
    fun getPrayerOfferingState() = prayerOfferingState
    fun setPrayerOfferingState(value: PrayerOfferingState?) { prayerOfferingState = value }
    fun clearPrayerOfferingState() { prayerOfferingState = null }
    fun getRunecraftingState() = runecraftingState
    fun setRunecraftingState(value: RunecraftingState?) { runecraftingState = value }
    fun clearRunecraftingState() { runecraftingState = null }
    fun getInteractionAnchorState() = interactionAnchorState
    fun setInteractionAnchorState(value: InteractionAnchorState?) { interactionAnchorState = value }
    fun clearInteractionAnchorState() { interactionAnchorState = null }
    fun getPyramidPlunderState() = pyramidPlunderState
    fun setPyramidPlunderState(value: PyramidPlunderPlayerState?) { pyramidPlunderState = value }
    fun clearPyramidPlunderState() { pyramidPlunderState = null }
    fun getMovementLockState() = movementLockState
    fun setMovementLockState(value: MovementLockState?) { movementLockState = value }
    fun clearMovementLockState() { movementLockState = null }
    fun getAgilitySessionState() = agilitySessionState
    fun setAgilitySessionState(value: AgilitySessionState?) { agilitySessionState = value }
    fun clearAgilitySessionState() { agilitySessionState = null }
    fun getSkillingEventState() = skillingEventState
    fun setSkillingEventState(value: SkillingEventState?) { skillingEventState = value }
    fun getPlayerPotatoState() = playerPotatoState
    fun setPlayerPotatoState(value: PlayerPotatoState?) { playerPotatoState = value }
    fun clearPlayerPotatoState() { playerPotatoState = null }

    fun getActiveSkillSessionKey() = activeSkillSessionKey
    fun setActiveSkillSession(key: String?, startedCycle: Long) { activeSkillSessionKey = key; activeSkillSessionStartedCycle = startedCycle }
    fun getActiveSkillSessionStartedCycle() = activeSkillSessionStartedCycle
    fun clearActiveSkillSession() { activeSkillSessionKey = null; activeSkillSessionStartedCycle = 0L }
    fun getPendingSkillMulti() = pendingSkillMulti
    fun setPendingSkillMulti(value: PendingSkillMulti?) { pendingSkillMulti = value }
    fun clearPendingSkillMulti() { pendingSkillMulti = null }
    fun getActiveActionHandle() = activeActionHandle
    fun setActiveActionHandle(value: QueueTaskHandle?) { activeActionHandle = value }
    fun getActiveActionType() = activeActionType
    fun setActiveActionType(value: PlayerActionType?) { activeActionType = value }
    fun getActionStartedCycle() = actionStartedCycle
    fun setActionStartedCycle(value: Long) { actionStartedCycle = value }
    fun cancelActiveAction() { activeActionHandle?.cancel() }
    fun clearActiveActionState() { activeActionHandle = null; activeActionType = null; actionStartedCycle = 0L; activeActionCancelReason = null }
    fun getActiveActionCancelReason() = activeActionCancelReason
    fun setActiveActionCancelReason(value: PlayerActionCancelReason?) { activeActionCancelReason = value }
    fun getLastActionCancelReason() = lastActionCancelReason
    fun setLastActionCancelReason(value: PlayerActionCancelReason?) { lastActionCancelReason = value }
    fun getLastActionCancelCycle() = lastActionCancelCycle
    fun setLastActionCancelCycle(value: Long) { lastActionCancelCycle = value }

    fun getCombatTargetState() = combatTargetState
    fun setCombatTargetState(value: CombatTargetState?) { combatTargetState = value }
    fun clearCombatTargetState() { combatTargetState = null }
    fun getCombatEngagementState() = combatEngagementState
    fun setCombatEngagementState(value: CombatEngagementState?) { combatEngagementState = value }
    fun clearCombatEngagementState() { combatEngagementState = null }
    fun getCombatCooldownState() = combatCooldownState
    fun setCombatCooldownState(value: CombatCooldownState?) { combatCooldownState = value }
    fun clearCombatCooldownState() { combatCooldownState = null }
    fun getAttackStartDedupeState() = attackStartDedupeState
    fun setAttackStartDedupeState(value: AttackStartDedupeState?) { attackStartDedupeState = value }
    fun clearAttackStartDedupeState() { attackStartDedupeState = null }
    fun getCombatCancellationReason() = combatCancellationReason
    fun setCombatCancellationReason(value: CombatCancellationReason?) { combatCancellationReason = value }
    fun clearCombatCancellationReason() { combatCancellationReason = null }
    fun getCombatLogoutLockUntilCycle() = combatLogoutLockUntilCycle
    fun setCombatLogoutLockUntilCycle(value: Long) { combatLogoutLockUntilCycle = value }
    fun getLastBlockAnimationCycle() = lastBlockAnimationCycle
    fun setLastBlockAnimationCycle(value: Long) { lastBlockAnimationCycle = value }
    fun getDeathTaskState() = deathTaskState
    fun setDeathTaskState(value: DeathTaskState?) { deathTaskState = value }
    fun clearDeathTaskState() { deathTaskState = null }

    fun getActiveSmithingSelection() = activeSmithingSelection
    fun setActiveSmithingSelection(value: ActiveSmithingSelection?) { activeSmithingSelection = value }
    fun clearActiveSmithingSelection() { activeSmithingSelection = null }
    fun getSmeltingSelection() = smeltingSelection
    fun setSmeltingSelection(value: SmeltingSelection?) { smeltingSelection = value }
    fun clearSmeltingSelection() { smeltingSelection = null }
    fun getPendingSmeltingBarId() = pendingSmeltingBarId
    fun setPendingSmeltingBarId(value: Int) { pendingSmeltingBarId = value }
    fun clearPendingSmeltingBarId() { pendingSmeltingBarId = -1 }
    fun getPendingProductionSelection() = pendingProductionSelection
    fun setPendingProductionSelection(value: PendingProductionSelection?) { pendingProductionSelection = value }
    fun clearPendingProductionSelection() { pendingProductionSelection = null }
    fun getActiveProductionSelection() = activeProductionSelection
    fun setActiveProductionSelection(value: ActiveProductionSelection?) { activeProductionSelection = value }
    fun clearActiveProductionSelection() { activeProductionSelection = null }
    fun getPlayerTaskSet() = playerTaskSet
    fun setPlayerTaskSet(value: GameTaskSet<*>?) { playerTaskSet = value }
    fun terminatePlayerTasks() {
        cancelActiveAction(); clearActiveActionState(); clearActiveSkillSession(); clearPendingSkillMulti()
        playerTaskSet?.terminateTasks(); playerTaskSet = null
    }
    fun getThrottleUntilCycle(key: String) = throttleUntilCycles.getOrDefault(key, 0L)
    fun setThrottleUntilCycle(key: String, cycle: Long) { if (cycle <= 0L) throttleUntilCycles.remove(key) else throttleUntilCycles[key] = cycle }
    fun clearThrottleUntilCycle(key: String) { throttleUntilCycles.remove(key) }

    private inline fun QueueTaskHandle?.cancelAndClear(clear: () -> Unit) { val handle = this; clear(); handle?.cancel() }
}
