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
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import net.dodian.uber.game.api.plugin.skills.startProduction
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import java.util.Collections
import java.util.WeakHashMap
import net.dodian.uber.skills.api.SkillMultiAction
import net.dodian.uber.skills.api.SkillMultiConfig
import net.dodian.uber.skills.api.SkillMultiEntry
import net.dodian.uber.skills.api.skillRecipe

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
        val short = skillRecipe("fletching.bow.$logIndex.short", bowLog.unstrungShortbowId) {
            material(bowLog.logItemId)
            requirement(bowLog.shortLevelRequired)
            experience(bowLog.shortExperience)
            animation(1248)
            delay(GameCycleClock.ticksForDurationMs(STANDARD_ACTION_DELAY_MS))
            success("You carefully cut the wood into a ${player.inventory.itemName(bowLog.unstrungShortbowId)}.")
        }
        val long = skillRecipe("fletching.bow.$logIndex.long", bowLog.unstrungLongbowId) {
            material(bowLog.logItemId)
            requirement(bowLog.longLevelRequired)
            experience(bowLog.longExperience)
            animation(1248)
            delay(GameCycleClock.ticksForDurationMs(STANDARD_ACTION_DELAY_MS))
            success("You carefully cut the wood into a ${player.inventory.itemName(bowLog.unstrungLongbowId)}.")
        }
        player.production.open(
            SkillMultiConfig(
                key = "fletching.bow.$logIndex",
                verb = "fletch",
                action = SkillMultiAction.CUT,
                entries = listOf(SkillMultiEntry(short), SkillMultiEntry(long)),
            ),
        ) { selection ->
            val selected = if (selection.recipeKey == long.key) long else short
            player.startProduction(selected, selection.amount, Skill.FLETCHING)
        }
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

object FletchingSkillPlugin : SkillPlugin {
    private val knifeIds = intArrayOf(946, 5605)

    override val definition =
        skillPlugin(name = "Fletching", skill = Skill.FLETCHING) {
            val logIds = FletchingData.bowLogs.map { it.logItemId }.distinct()
            for (knifeId in knifeIds) {
                for (logId in logIds) {
                    itemOnItem(preset = PolicyPreset.PRODUCTION, leftItemId = knifeId, rightItemId = logId) { interaction ->
                val itemUsed = interaction.itemUsed
                val otherItem = interaction.otherItem
                        val usedLogId = if (itemUsed == knifeId) otherItem else itemUsed
                        val logIndex = FletchingData.bowLogs.indexOfFirst { it.logItemId == usedLogId }
                        if (logIndex < 0) {
                            false
                        } else {
                            Fletching.open(interaction.player, logIndex)
                            true
                        }
                    }
                }
            }
        }
}
