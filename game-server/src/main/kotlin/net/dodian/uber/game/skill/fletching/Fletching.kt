package net.dodian.uber.game.skill.fletching

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.loop.GameCycleClock
import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.engine.systems.skills.asSkillPlayer
import net.dodian.uber.game.skill.runtime.action.ActionStopReason
import net.dodian.uber.game.skill.runtime.action.RunningProductionAction
import net.dodian.uber.game.skill.runtime.action.SkillingRandomEventService
import net.dodian.uber.game.skill.runtime.action.productionAction
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import java.util.Collections
import java.util.WeakHashMap
import net.dodian.uber.skills.fletching.FletchingModule

object Fletching {
    private const val STANDARD_ACTION_DELAY_MS = 1800L
    private val fletchingTasks = Collections.synchronizedMap(WeakHashMap<Client, RunningProductionAction>())

    @JvmStatic
    fun open(client: Client, logIndex: Int) = openBowSelection(client.asSkillPlayer(), logIndex)

    fun open(player: SkillPlayer, logIndex: Int) = openBowSelection(player, logIndex)

    @JvmStatic
    fun start(client: Client, longBow: Boolean, amount: Int) = startBowCrafting(client, longBow, amount)

    @JvmStatic
    fun openBowSelection(player: SkillPlayer, logIndex: Int) {
        val bowLog = FletchingData.bowLog(logIndex) ?: return
        FletchingModule.openBowSelection(player, bowLog.logItemId)
    }

    @JvmStatic
    fun startBowCrafting(client: Client, longBow: Boolean, amount: Int) {
        client.send(RemoveInterfaces())
        val logIndex = client.fletchingState?.logIndex ?: -1
        val bowLog = FletchingData.bowLog(logIndex)
        if (bowLog == null) {
            client.resetAction()
            return
        }

        val request =
        if (longBow) {
            if (client.getLevel(Skill.FLETCHING) < bowLog.longLevelRequired) {
                client.sendMessage("Requires fletching ${bowLog.longLevelRequired}!")
                client.resetAction()
                return
            }
            FletchingRequest(logIndex, bowLog.unstrungLongbowId, bowLog.longExperience, amount)
        } else {
            if (client.getLevel(Skill.FLETCHING) < bowLog.shortLevelRequired) {
                client.sendMessage("Requires fletching ${bowLog.shortLevelRequired}!")
                client.resetAction()
                return
            }
            FletchingRequest(logIndex, bowLog.unstrungShortbowId, bowLog.shortExperience, amount)
        }

        start(client, request)
    }

    @JvmStatic
    fun start(client: Client, request: FletchingRequest) {
        client.fletchingState =
            FletchingState(
                logIndex = request.logIndex,
                productId = request.productId,
                experience = request.experience,
                remaining = request.amount,
            )
        startAction(client)
    }

    @JvmStatic
    fun startAction(client: Client) {
        stopAction(client, ActionStopReason.USER_INTERRUPT)
        val action =
            productionAction("fletching") {
                delay { GameCycleClock.ticksForDurationMs(STANDARD_ACTION_DELAY_MS) }
                onCycleWhile {
                    if ((client.fletchingState?.remaining ?: 0) <= 0) {
                        return@onCycleWhile false
                    }
                    performBowCycle(client)
                    (client.fletchingState?.remaining ?: 0) > 0
                }
                onStop {
                    fletchingTasks.remove(client)
                    client.clearFletchingState()
                }
            }
        val running = action.start(client.asSkillPlayer()) ?: run {
            client.clearFletchingState()
            return
        }
        fletchingTasks[client] = running
    }

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun stopFromReset(client: Client, fullReset: Boolean) {
        stopAction(client, ActionStopReason.USER_INTERRUPT)
    }

    @JvmStatic
    fun stopAction(
        client: Client,
        reason: ActionStopReason = ActionStopReason.USER_INTERRUPT,
    ) {
        fletchingTasks.remove(client)?.cancel(reason)
    }

    @JvmStatic
    fun performBowCycle(client: Client) {
        val state = client.fletchingState ?: run {
            client.resetAction()
            return
        }
        if (state.remaining < 1) {
            client.resetAction()
            return
        }
        if (client.isBusy) {
            client.sendMessage("You are currently busy to be fletching!")
            return
        }

        client.send(RemoveInterfaces())
        client.IsBanking = false
        client.performAnimation(4433, 0)

        val logIndex = state.logIndex
        val bowLog = FletchingData.bowLog(logIndex)
        if (bowLog == null || !client.playerHasItem(bowLog.logItemId)) {
            client.resetAction()
            return
        }

        client.deleteItem(bowLog.logItemId, 1)
        client.addItem(state.productId, 1)
        client.checkItemUpdate()
        ProgressionService.addXp(client, state.experience, Skill.FLETCHING)
        SkillingRandomEventService.trigger(client, state.experience)
        client.fletchingState = state.copy(remaining = state.remaining - 1)
    }
}
