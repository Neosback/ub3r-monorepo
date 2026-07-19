package net.dodian.uber.game.skill.cooking

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.Server
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.loop.GameCycleClock
import net.dodian.uber.game.engine.systems.skills.ProgressionService
import net.dodian.uber.game.engine.systems.skills.asSkillPlayer
import net.dodian.uber.game.skill.runtime.action.ActionStopReason
import net.dodian.uber.game.skill.runtime.action.RunningProductionAction
import net.dodian.uber.game.skill.runtime.action.SkillingRandomEventService
import net.dodian.uber.game.skill.runtime.action.productionAction
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.netty.listener.out.SendFrame27
import java.util.Collections
import java.util.WeakHashMap

object Cooking {
    private const val STANDARD_ACTION_DELAY_MS = 1800L
    private val cookingTasks = Collections.synchronizedMap(WeakHashMap<Client, RunningProductionAction>())

    @JvmStatic
    fun start(client: Client, itemId: Int) {
        if (client.isBusy) {
            client.sendMessage("You are currently busy to be cooking!")
            return
        }
        val recipe = CookingData.findRecipe(itemId) ?: run {
            return
        }
        start(client, CookingRequest(itemId, CookingData.recipes.indexOf(recipe), client.getInvAmt(itemId)))
    }

    @JvmStatic
    fun start(client: Client, request: CookingRequest) {
        client.cookingState = CookingState(request.itemId, request.cookIndex, request.amount)
        startAction(client)
    }

    @JvmStatic
    fun startAction(client: Client) {
        stopAction(client, ActionStopReason.USER_INTERRUPT)
        val action =
            productionAction("cooking") {
                delay { GameCycleClock.ticksForDurationMs(STANDARD_ACTION_DELAY_MS) }
                onCycleWhile {
                    if ((client.cookingState?.remaining ?: 0) <= 0) {
                        return@onCycleWhile false
                    }
                    performCycle(client)
                    (client.cookingState?.remaining ?: 0) > 0
                }
                onStop {
                    cookingTasks.remove(client)
                    client.clearCookingState()
                }
            }
        val running = action.start(client.asSkillPlayer()) ?: run {
            client.clearCookingState()
            return
        }
        cookingTasks[client] = running
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
        cookingTasks.remove(client)?.cancel(reason)
    }

    @JvmStatic
    fun attempt(client: Client, itemId: Int) = start(client, itemId)

    /**
     * Entry point for both "use item on range" and "click range" — cooks a single item
     * outright when that's all the player is carrying, otherwise stashes the pending
     * recipe and opens the "how many would you like to cook?" chatbox instead of
     * silently cooking the player's entire inventory stack.
     */
    @JvmStatic
    fun promptQuantity(client: Client, itemId: Int): Boolean {
        if (client.isBusy) {
            client.sendMessage("You are currently busy to be cooking!")
            return true
        }
        val recipe = CookingData.findRecipe(itemId) ?: return false
        val available = client.getInvAmt(itemId)
        if (available <= 0) {
            return false
        }
        if (available == 1) {
            start(client, itemId)
            return true
        }
        client.cookingState = CookingState(itemId, CookingData.recipes.indexOf(recipe), remaining = 0)
        client.enterAmountId = 1
        client.send(SendFrame27("How many would you like to cook?"))
        return true
    }

    /** Finds the first raw item in the player's inventory that this recipe table can cook. */
    @JvmStatic
    fun firstCookableInInventory(client: Client): Int =
        CookingData.recipes.map { it.rawItemId }.firstOrNull { client.getInvAmt(it) > 0 } ?: -1

    @JvmStatic
    fun startFromEnteredAmount(client: Client, amount: Int) {
        val current = client.cookingState ?: return
        start(client, CookingRequest(current.itemId, current.cookIndex, amount))
    }

    @JvmStatic
    fun performCycle(client: Client) {
        val state = client.cookingState
        if (client.isBusy || state == null || state.remaining < 1) {
            client.resetAction(true)
            return
        }
        val cookIndex = state.cookIndex
        val itemId = state.itemId
        val recipe = CookingData.recipeByIndex(cookIndex) ?: run {
            client.resetAction(true)
            return
        }
        if (!client.playerHasItem(itemId)) {
            client.sendMessage("You are out of fish")
            client.resetAction(true)
            return
        }
        if (client.getLevel(Skill.COOKING) < recipe.requiredLevel) {
            client.sendMessage("You need ${recipe.requiredLevel} cooking to cook the ${Server.itemManager.getName(itemId).lowercase()}.")
            client.resetAction(true)
            return
        }

        var ran = recipe.burnRollBase - client.getLevel(Skill.COOKING)
        if (client.equipment[Equipment.Slot.HANDS.id] == 775) ran -= 4
        if (client.equipment[Equipment.Slot.HEAD.id] == 1949) ran -= 4
        if (client.equipment[Equipment.Slot.HEAD.id] == 1949 && client.equipment[Equipment.Slot.HANDS.id] == 775) ran -= 2
        ran = ran.coerceIn(0, 100)
        val burn = 1 + net.dodian.utilities.Utils.random(99) <= ran

        if (recipe.experience <= 0) {
            client.resetAction(true)
            return
        }
        client.cookingState = state.copy(remaining = state.remaining - 1)
        client.deleteItem(itemId, 1)
        client.setFocus(client.interactionAnchorX, client.interactionAnchorY)
        client.performAnimation(883, 0)
        if (!burn) {
            client.addItem(recipe.cookedItemId, 1)
            client.sendMessage("You cook the ${client.getItemName(itemId)}")
            ProgressionService.addXp(client, recipe.experience, Skill.COOKING)
        } else {
            client.addItem(recipe.burntItemId, 1)
            client.sendMessage("You burn the ${client.getItemName(itemId)}")
        }
        client.checkItemUpdate()
        SkillingRandomEventService.trigger(client, recipe.experience)
    }
}

object CookingSkillPlugin : SkillPlugin {
    override val definition =
        skillPlugin(name = "Cooking", skill = Skill.COOKING) {
            val rangeObjectIds = intArrayOf(26181, 114, 4172)
            itemOnObject(preset = PolicyPreset.PRODUCTION, objectIds = rangeObjectIds) { interaction ->
                val objectId = interaction.objectId
                val position = interaction.position
                val itemId = interaction.itemId
                if (objectId == 26181 && itemId == 401) {
                    val amount = interaction.player.inventory.amount(401)
                    val committed = amount > 0 && interaction.player.inventory.transaction {
                        remove(401, amount)
                        add(1781, amount)
                    }
                    if (committed) interaction.player.ui.message("You burn all your seaweed into ashes.")
                    true
                } else {
                    val client = net.dodian.uber.game.engine.systems.skills.SkillEngineAccess.client(interaction.player)
                    client.setInteractionAnchor(position.x, position.y, position.z)
                    Cooking.promptQuantity(client, itemId)
                    true
                }
            }

            objectClick(preset = PolicyPreset.PRODUCTION, option = 1, *rangeObjectIds) { interaction ->
                val client = net.dodian.uber.game.engine.systems.skills.SkillEngineAccess.client(interaction.player)
                val position = interaction.position
                val itemId = Cooking.firstCookableInInventory(client)
                if (itemId == -1) {
                    client.sendMessage("You don't have anything to cook.")
                } else {
                    client.setInteractionAnchor(position.x, position.y, position.z)
                    Cooking.promptQuantity(client, itemId)
                }
                true
            }
        }
}
