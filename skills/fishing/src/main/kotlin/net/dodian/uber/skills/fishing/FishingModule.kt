package net.dodian.uber.skills.fishing

import net.dodian.uber.game.api.content.ContentAttributeKey
import net.dodian.uber.game.api.plugin.ContentMaturity
import net.dodian.uber.game.api.plugin.ContentModuleManifest
import net.dodian.uber.game.api.plugin.skills.SkillActionHandle
import net.dodian.uber.game.api.plugin.skills.SkillEquipmentSlot
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.SkillPluginDefinition
import net.dodian.uber.game.api.plugin.skills.manifest
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.skill.runtime.action.ActionStopReason
import net.dodian.uber.game.skill.runtime.action.CycleSignal
import net.dodian.uber.game.skill.runtime.action.gatheringAction
import net.dodian.uber.skills.api.SkillModuleDescriptor
import net.dodian.uber.skills.runtime.TomlRecordReader

data class FishingSpotDef(
    val index: Int,
    val npcId: Int,
    val clickOption: Int,
    val fishItemId: Int,
    val animationId: Int,
    val requiredLevel: Int,
    val baseDelayMs: Int,
    val toolItemId: Int,
    val experience: Int,
    val premiumOnly: Boolean = false,
    val featherConsumed: Boolean = false,
    val bigCatchItemId: Int = -1,
    val bigCatchRequiredLevel: Int = 0,
    val bigCatchBonusXp: Int = 0,
)

/** Complete plugin-owned reference implementation for fishing spots. */
object FishingModule : SkillPlugin {
    val descriptor = SkillModuleDescriptor("skill.fishing", "Fishing")

    private const val TICK_MS = 600L
    private const val ANIMATION_REAPPLY_MS = 1800L
    private const val HARPOON_ITEM_ID = 21028
    private const val HARPOON_LEVEL = 61
    private const val FEATHER_ITEM_ID = 314
    private const val REST_THRESHOLD = 4

    private val gatheredKey = ContentAttributeKey<Int>("skill.fishing", "gathered")
    private val ticksUntilCatchKey = ContentAttributeKey<Int>("skill.fishing", "ticksUntilCatch")
    private val ticksUntilAnimationKey = ContentAttributeKey<Int>("skill.fishing", "ticksUntilAnimation")
    private val activeActionKey = ContentAttributeKey<SkillActionHandle>("skill.fishing", "activeAction")

    val spots: List<FishingSpotDef> by lazy { loadSpots() }

    override val definition: SkillPluginDefinition = skillPlugin("Fishing", Skill.FISHING) {
        val byOption = spots.groupBy { it.clickOption }
        val firstNpcIds = byOption[1].orEmpty().map { it.npcId }.distinct().toIntArray()
        val secondNpcIds = byOption[2].orEmpty().map { it.npcId }.distinct().toIntArray()

        if (firstNpcIds.isNotEmpty()) {
            npcClick(preset = PolicyPreset.GATHERING, option = 1, *firstNpcIds) { interaction ->
                attempt(interaction.player, interaction.npc.id, 1)
                true
            }
        }
        if (secondNpcIds.isNotEmpty()) {
            npcClick(preset = PolicyPreset.GATHERING, option = 2, *secondNpcIds) { interaction ->
                attempt(interaction.player, interaction.npc.id, 2)
                true
            }
        }
    }

    override val contentManifest: ContentModuleManifest = definition.manifest(
        id = descriptor.id,
        owner = "gameplay",
        version = descriptor.version,
        maturity = ContentMaturity.STABLE,
    )

    fun attempt(player: SkillPlayer, npcId: Int, clickOption: Int) {
        val spot = spots.firstOrNull { it.npcId == npcId && it.clickOption == clickOption } ?: return
        if (player.inventory.freeSlots() <= 0) {
            player.ui.message("Not enough inventory space.")
            return
        }
        if (player.skills.current(Skill.FISHING) < spot.requiredLevel) {
            player.ui.message("You need level ${spot.requiredLevel} fishing to fish here.")
            return
        }
        if (!player.inventory.contains(spot.toolItemId) && !hasHarpoon(player)) {
            player.ui.message("You need a ${player.inventory.itemName(spot.toolItemId).lowercase()} to fish here.")
            return
        }
        if (spot.premiumOnly && !player.profile.premium) {
            player.ui.message("You need to be premium to fish from this spot!")
            return
        }
        if (spot.featherConsumed && !player.inventory.contains(FEATHER_ITEM_ID)) {
            player.ui.message("You do not have any feathers.")
            return
        }

        stopAction(player)
        player.attributes.put(gatheredKey, 0)
        player.attributes.put(ticksUntilCatchKey, toTicks(catchDelayMs(player, spot)))
        player.attributes.put(ticksUntilAnimationKey, toTicks(ANIMATION_REAPPLY_MS))
        player.actions.animate(spot.animationId, 0)
        player.ui.message("You start fishing...")

        val handle = gatheringAction("fishing") {
            delay(1)
            onCycleSignal { fishOnce(this, spot) }
            onStop {
                player.attributes.remove(gatheredKey)
                player.attributes.remove(ticksUntilCatchKey)
                player.attributes.remove(ticksUntilAnimationKey)
                player.attributes.remove(activeActionKey)
            }
        }.start(player)
        if (handle == null) {
            player.attributes.remove(gatheredKey)
            player.attributes.remove(ticksUntilCatchKey)
            player.attributes.remove(ticksUntilAnimationKey)
            return
        }
        player.attributes.put(activeActionKey, handle)
    }

    fun stopAction(player: SkillPlayer, reason: ActionStopReason = ActionStopReason.USER_INTERRUPT) {
        player.attributes.get(activeActionKey)?.cancel(reason)
        player.attributes.remove(activeActionKey)
    }

    private fun hasHarpoon(player: SkillPlayer): Boolean =
        player.skills.current(Skill.FISHING) >= HARPOON_LEVEL &&
            (player.equipment.item(SkillEquipmentSlot.WEAPON) == HARPOON_ITEM_ID || player.inventory.contains(HARPOON_ITEM_ID))

    private fun catchDelayMs(player: SkillPlayer, spot: FishingSpotDef): Long {
        val levelBonus = player.skills.current(Skill.FISHING) / 256.0
        val harpoon = hasHarpoon(player)
        val bonus = 1 + levelBonus + (if (harpoon) 0.2 else 0.0)
        var timer = spot.baseDelayMs.toDouble()
        if (harpoon && player.random.chance(1, 8)) timer -= 600
        return (timer / bonus).toLong()
    }

    private fun toTicks(ms: Long): Int = if (ms <= 0L) 1 else ((ms + TICK_MS - 1) / TICK_MS).toInt().coerceAtLeast(1)

    private fun fishOnce(player: SkillPlayer, spot: FishingSpotDef): CycleSignal {
        if (!player.inventory.contains(spot.toolItemId) && !hasHarpoon(player)) {
            player.ui.message("You need a ${player.inventory.itemName(spot.toolItemId).lowercase()} to fish here.")
            return CycleSignal.stop()
        }
        if (player.inventory.freeSlots() <= 0) {
            player.ui.message("Not enough inventory space.")
            return CycleSignal.stop()
        }
        if (spot.featherConsumed && !player.inventory.contains(FEATHER_ITEM_ID)) {
            player.ui.message("You do not have any feathers.")
            return CycleSignal.stop()
        }

        val ticksUntilAnimation = (player.attributes.get(ticksUntilAnimationKey) ?: 0) - 1
        if (ticksUntilAnimation <= 0) {
            player.actions.animate(spot.animationId, 0)
            player.attributes.put(ticksUntilAnimationKey, toTicks(ANIMATION_REAPPLY_MS))
        } else {
            player.attributes.put(ticksUntilAnimationKey, ticksUntilAnimation)
        }

        val ticksUntilCatch = (player.attributes.get(ticksUntilCatchKey) ?: 0) - 1
        if (ticksUntilCatch > 0) {
            player.attributes.put(ticksUntilCatchKey, ticksUntilCatch)
            return CycleSignal.continueWithoutSuccess()
        }

        val bigCatch = spot.bigCatchItemId >= 0 &&
            player.skills.current(Skill.FISHING) >= spot.bigCatchRequiredLevel &&
            player.random.between(0, 6) < 3
        val itemId = if (bigCatch) spot.bigCatchItemId else spot.fishItemId
        val experience = if (bigCatch) spot.experience + spot.bigCatchBonusXp else spot.experience

        if (spot.featherConsumed) player.inventory.transaction { remove(FEATHER_ITEM_ID, 1) }
        player.inventory.add(itemId, 1)
        player.actions.logGathering(itemId, 1, "Fishing")
        player.skills.gainXp(experience, Skill.FISHING)
        player.actions.animate(spot.animationId, 0)
        player.actions.triggerRandomEvent(experience)
        player.ui.message("You fish up some ${player.inventory.itemName(itemId).lowercase().replace("raw ", "")}.")

        val gathered = (player.attributes.get(gatheredKey) ?: 0) + 1
        player.attributes.put(gatheredKey, gathered)
        player.attributes.put(ticksUntilCatchKey, toTicks(catchDelayMs(player, spot)))
        player.attributes.put(ticksUntilAnimationKey, toTicks(ANIMATION_REAPPLY_MS))

        if (gathered >= REST_THRESHOLD && player.random.chance(1, 20)) {
            player.ui.message("You take a rest after gathering $gathered resources.")
            return CycleSignal.stop(ActionStopReason.COMPLETED)
        }
        return CycleSignal.success()
    }

    private fun loadSpots(): List<FishingSpotDef> =
        TomlRecordReader.readRecords("fishing/spots.toml", "spot").map { row ->
            FishingSpotDef(
                index = row.getValue("index").toInt(),
                npcId = row.getValue("npcId").toInt(),
                clickOption = row.getValue("clickOption").toInt(),
                fishItemId = row.getValue("fishItemId").toInt(),
                animationId = row.getValue("animationId").toInt(),
                requiredLevel = row.getValue("requiredLevel").toInt(),
                baseDelayMs = row.getValue("baseDelayMs").toInt(),
                toolItemId = row.getValue("toolItemId").toInt(),
                experience = row.getValue("experience").toInt(),
                premiumOnly = row["premiumOnly"]?.toBoolean() ?: false,
                featherConsumed = row["featherConsumed"]?.toBoolean() ?: false,
                bigCatchItemId = row["bigCatchItemId"]?.toInt() ?: -1,
                bigCatchRequiredLevel = row["bigCatchRequiredLevel"]?.toInt() ?: 0,
                bigCatchBonusXp = row["bigCatchBonusXp"]?.toInt() ?: 0,
            )
        }
}
