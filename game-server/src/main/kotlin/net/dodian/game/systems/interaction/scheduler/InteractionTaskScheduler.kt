package net.dodian.game.systems.interaction.scheduler

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.systems.interaction.InteractionIntent
import net.dodian.game.systems.interaction.GroundItemInteractionIntent
import net.dodian.game.systems.interaction.ItemOnObjectIntent
import net.dodian.game.systems.interaction.MagicOnObjectIntent
import net.dodian.game.systems.interaction.NpcInteractionIntent
import net.dodian.game.systems.interaction.ObjectClickIntent
import net.dodian.game.systems.interaction.PlayerInteractionIntent
import net.dodian.game.systems.action.PlayerActionCancellationService
import net.dodian.game.systems.action.PlayerActionCancelReason
import net.dodian.game.engine.loop.GameCycleClock
import net.dodian.game.engine.scheduler.QueueTaskHandle
import net.dodian.game.engine.tasking.GameTaskRuntime
import net.dodian.game.engine.tasking.TaskPriority

object InteractionTaskScheduler {
    @JvmStatic
    fun schedule(player: Client, intent: InteractionIntent, task: InteractionQueueTask) {
        player.cancelInteractionTask()
        PlayerActionCancellationService.cancel(
            player = player,
            reason = cancelReason(intent),
            fullResetAnimation = false,
            resetCompatibilityState = true,
        )
        player.pendingInteraction = intent
        player.activeInteraction = null
        player.interactionEarliestCycle = GameCycleClock.currentCycle()
        val handle =
            GameTaskRuntime.queuePlayer(player, TaskPriority.STANDARD) {
                while (task.execute()) {
                    wait(1)
                }
            }
        player.interactionTaskHandle = QueueTaskHandle.from(handle)
    }

    private fun cancelReason(intent: InteractionIntent): PlayerActionCancelReason =
        when (intent) {
            is ObjectClickIntent,
            is ItemOnObjectIntent,
            is MagicOnObjectIntent -> PlayerActionCancelReason.OBJECT_INTERACTION
            is NpcInteractionIntent ->
                if (intent.option == 5) {
                    PlayerActionCancelReason.NEW_ACTION
                } else {
                    PlayerActionCancelReason.NPC_INTERACTION
                }
            is PlayerInteractionIntent -> PlayerActionCancelReason.PLAYER_INTERACTION
            is GroundItemInteractionIntent -> PlayerActionCancelReason.GROUND_ITEM_INTERACTION
            else -> PlayerActionCancelReason.NEW_ACTION
        }
}
