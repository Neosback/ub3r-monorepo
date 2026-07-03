package net.dodian.uber.game.engine.systems.net

import net.dodian.uber.game.skill.smithing.SmithingData
import net.dodian.uber.game.model.entity.player.Client

object PacketButtonService {

    private val combatConfigButtons: Set<Int> by lazy {
        val styleIds = net.dodian.uber.game.ui.CombatInterface.bindings.flatMap { it.rawButtonIds.toList() }
        styleIds.toSet()
    }

    @JvmStatic
    fun recordLastActionIndex(client: Client, actionIndex: Int) {
        client.lastButtonActionIndex = actionIndex
    }

    @JvmStatic
    fun isSmeltingInterfaceActive(client: Client): Boolean = client.activeInterfaceId == 2400

    @JvmStatic
    fun prepareAction(client: Client, actionButton: Int) {
        if (!(actionButton >= 9157 && actionButton <= 9194)) {
            client.actionButtonId = actionButton
        }
        val preserveSmeltingSelection = client.activeInterfaceId == 2400 &&
            SmithingData.isSmeltingInterfaceButton(actionButton)
        val preserveSmithingSelection = client.activeInterfaceId in 1119..1123
        val preserveCombatConfig = actionButton in combatConfigButtons
        if (!preserveSmeltingSelection && !preserveSmithingSelection && !preserveCombatConfig &&
            actionButton != 10239 && actionButton != 10238 &&
            actionButton != 6212 && actionButton != 6211
        ) {
            client.resetAction(false)
        }
    }
}
