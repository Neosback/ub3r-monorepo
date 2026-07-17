package net.dodian.uber.game.engine.systems.skills

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.skills.api.SkillMultiSelection

/** Normalizes the active 317 make-1/5/10/all button layouts. */
object SkillMultiButtonService {
    private val singleAmounts = mapOf(10239 to 1, 10238 to 5, 6212 to 10, 6211 to Int.MAX_VALUE)
    private val twoEntryButtons = buildMap {
        intArrayOf(34174, 34173, 34172, 34171).forEachIndexed { i, id -> put(id, 0 to amount(i)) }
        intArrayOf(34170, 34169, 34168, 34167).forEachIndexed { i, id -> put(id, 1 to amount(i)) }
    }
    private val threeEntryButtons = buildMap {
        listOf(
            intArrayOf(34185, 34184, 34183, 34182),
            intArrayOf(34189, 34188, 34187, 34186),
            intArrayOf(34193, 34192, 34191, 34190),
        ).forEachIndexed { entry, ids -> ids.forEachIndexed { i, id -> put(id, entry to amount(i)) } }
    }

    @JvmStatic
    fun tryHandle(client: Client, buttonId: Int): Boolean {
        val player = client.asSkillPlayer()
        val config = player.production.pending() ?: return false
        val (entryIndex, amount) = when {
            buttonId in singleAmounts -> 0 to singleAmounts.getValue(buttonId)
            buttonId in twoEntryButtons -> twoEntryButtons.getValue(buttonId)
            buttonId in threeEntryButtons -> threeEntryButtons.getValue(buttonId)
            else -> return false
        }
        val entry = config.entries.getOrNull(entryIndex) ?: run {
            player.production.clear()
            return false
        }
        return player.production.select(SkillMultiSelection(config.key, entry.recipe.key, amount))
    }

    @JvmStatic
    fun isSelectionButton(buttonId: Int): Boolean =
        buttonId in singleAmounts || buttonId in twoEntryButtons || buttonId in threeEntryButtons

    private fun amount(index: Int) = when (index) { 0 -> 1; 1 -> 5; 2 -> 10; else -> Int.MAX_VALUE }
}
