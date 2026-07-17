package net.dodian.uber.skills.fletching

import net.dodian.uber.game.api.plugin.ContentMaturity
import net.dodian.uber.game.api.plugin.ContentModuleManifest
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.SkillPluginDefinition
import net.dodian.uber.game.api.plugin.skills.manifest
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.api.plugin.skills.startProduction
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.api.SkillModuleDescriptor
import net.dodian.uber.skills.api.SkillMultiAction
import net.dodian.uber.skills.api.SkillMultiConfig
import net.dodian.uber.skills.api.SkillMultiEntry
import net.dodian.uber.skills.api.SkillRecipe
import net.dodian.uber.skills.api.skillRecipe

data class FletchingLogDefinition(
    val logItemId: Int,
    val unstrungShortbowId: Int,
    val unstrungLongbowId: Int,
    val shortbowId: Int,
    val longbowId: Int,
    val shortLevelRequired: Int,
    val longLevelRequired: Int,
    val shortExperience: Int,
    val longExperience: Int,
    val shortStringAnimationId: Int,
    val longStringAnimationId: Int,
)

data class FletchingAmmoDefinition(val materialId: Int, val productId: Int, val level: Int, val experience: Int)

/** Complete plugin-owned reference implementation for production skills. */
object FletchingModule : SkillPlugin {
    val descriptor = SkillModuleDescriptor("skill.fletching", "Fletching")
    val knifeIds: IntArray = intArrayOf(946, 5605)
    val bowLogs: List<FletchingLogDefinition> = listOf(
        FletchingLogDefinition(1521, 54, 56, 843, 845, 20, 25, 102, 150, 6679, 6685),
        FletchingLogDefinition(1519, 60, 58, 849, 847, 35, 40, 198, 252, 6680, 6686),
        FletchingLogDefinition(1517, 64, 62, 853, 851, 50, 55, 300, 348, 6681, 6687),
        FletchingLogDefinition(1515, 68, 66, 857, 855, 65, 70, 408, 450, 6682, 6688),
        FletchingLogDefinition(1513, 72, 70, 861, 859, 80, 85, 504, 552, 6683, 6689),
    )
    private val arrowHeads = listOf(
        FletchingAmmoDefinition(39, 882, 1, 7), FletchingAmmoDefinition(40, 884, 15, 13),
        FletchingAmmoDefinition(41, 886, 30, 25), FletchingAmmoDefinition(42, 888, 45, 38),
        FletchingAmmoDefinition(43, 890, 60, 50), FletchingAmmoDefinition(44, 892, 75, 63),
        FletchingAmmoDefinition(11237, 11212, 90, 75),
    )
    private val dartTips = listOf(
        FletchingAmmoDefinition(819, 806, 1, 18), FletchingAmmoDefinition(820, 807, 22, 38),
        FletchingAmmoDefinition(821, 808, 37, 75), FletchingAmmoDefinition(822, 809, 52, 112),
        FletchingAmmoDefinition(823, 810, 67, 150), FletchingAmmoDefinition(824, 811, 81, 188),
    )

    override val definition: SkillPluginDefinition = skillPlugin("Fletching", Skill.FLETCHING) {
        knifeIds.forEach { knifeId ->
            bowLogs.forEach { log ->
                itemOnItem(PolicyPreset.PRODUCTION, knifeId, log.logItemId) { interaction ->
                    openBowSelection(interaction.player, log)
                }
            }
        }
        knifeIds.forEach { knifeId ->
            itemOnItem(PolicyPreset.PRODUCTION, knifeId, 1511) { interaction ->
                openSingle(interaction.player, shaftRecipe(), "fletch")
            }
        }
        itemOnItem(PolicyPreset.PRODUCTION, 52, 314) { interaction ->
            openSingle(interaction.player, headlessArrowRecipe(), "fletch")
        }
        arrowHeads.forEach { arrow ->
            itemOnItem(PolicyPreset.PRODUCTION, 53, arrow.materialId) { interaction ->
                openSingle(interaction.player, arrowRecipe(arrow), "fletch")
            }
        }
        dartTips.forEach { dart ->
            itemOnItem(PolicyPreset.PRODUCTION, 314, dart.materialId) { interaction ->
                openSingle(interaction.player, dartRecipe(dart), "fletch")
            }
        }
        bowLogs.forEach { log ->
            itemOnItem(PolicyPreset.PRODUCTION, 1777, log.unstrungShortbowId) { interaction ->
                openSingle(interaction.player, stringRecipe(log, longbow = false), "string")
            }
            itemOnItem(PolicyPreset.PRODUCTION, 1777, log.unstrungLongbowId) { interaction ->
                openSingle(interaction.player, stringRecipe(log, longbow = true), "string")
            }
        }
    }

    override val contentManifest: ContentModuleManifest = definition.manifest(
        id = descriptor.id,
        owner = "gameplay",
        version = descriptor.version,
        maturity = ContentMaturity.STABLE,
    )

    fun openBowSelection(player: SkillPlayer, logItemId: Int): Boolean =
        bowLogs.firstOrNull { it.logItemId == logItemId }?.let { openBowSelection(player, it) } ?: false

    fun openBowSelection(player: SkillPlayer, log: FletchingLogDefinition): Boolean {
        val short = recipe(player, log, longbow = false)
        val long = recipe(player, log, longbow = true)
        return player.production.open(
            SkillMultiConfig(
                key = "fletching.bow.${log.logItemId}",
                verb = "fletch",
                action = SkillMultiAction.CUT,
                entries = listOf(SkillMultiEntry(short), SkillMultiEntry(long)),
            ),
        ) { selection ->
            val selected = if (selection.recipeKey == long.key) long else short
            player.startProduction(selected, selection.amount, Skill.FLETCHING)
        }
    }

    private fun openSingle(player: SkillPlayer, recipe: SkillRecipe, verb: String): Boolean =
        player.production.open(
            SkillMultiConfig(
                key = "${recipe.key}.menu",
                verb = verb,
                action = SkillMultiAction.MAKE,
                entries = listOf(SkillMultiEntry(recipe)),
            ),
        ) { selection -> player.startProduction(recipe, selection.amount, Skill.FLETCHING) }

    private fun recipe(player: SkillPlayer, log: FletchingLogDefinition, longbow: Boolean): SkillRecipe {
        val productId = if (longbow) log.unstrungLongbowId else log.unstrungShortbowId
        val level = if (longbow) log.longLevelRequired else log.shortLevelRequired
        val experience = if (longbow) log.longExperience else log.shortExperience
        val kind = if (longbow) "long" else "short"
        return skillRecipe("fletching.bow.${log.logItemId}.$kind", productId) {
            material(log.logItemId)
            requirement(level)
            experience(experience)
            animation(4433)
            delay(3)
            success("You carefully cut the wood into a ${player.inventory.itemName(productId)}.")
            missingMaterials("You need another log to continue fletching.")
        }
    }

    private fun shaftRecipe(): SkillRecipe = skillRecipe("fletching.shafts", 52) {
        output(15); material(1511); requirement(1); experience(5); animation(4433); delay(2)
        success("You carefully cut the wood into arrow shafts."); missingMaterials("You need another log to continue fletching.")
    }

    private fun headlessArrowRecipe(): SkillRecipe = skillRecipe("fletching.headless-arrows", 53) {
        output(15); material(52); material(314); requirement(1); experience(5); delay(2)
        success("You attach feathers to the arrow shafts.")
    }

    private fun arrowRecipe(definition: FletchingAmmoDefinition): SkillRecipe =
        skillRecipe("fletching.arrow.${definition.productId}", definition.productId) {
            output(15); material(53); material(definition.materialId); requirement(definition.level)
            experience(definition.experience); delay(3); success("You fletch some arrows.")
        }

    private fun dartRecipe(definition: FletchingAmmoDefinition): SkillRecipe =
        skillRecipe("fletching.dart.${definition.productId}", definition.productId) {
            output(10); material(314); material(definition.materialId); requirement(definition.level)
            experience(definition.experience / 2); delay(3); success("You fletch some darts.")
        }

    private fun stringRecipe(log: FletchingLogDefinition, longbow: Boolean): SkillRecipe {
        val input = if (longbow) log.unstrungLongbowId else log.unstrungShortbowId
        val output = if (longbow) log.longbowId else log.shortbowId
        val level = if (longbow) log.longLevelRequired else log.shortLevelRequired
        val experience = if (longbow) log.longExperience else log.shortExperience
        val animation = if (longbow) log.longStringAnimationId else log.shortStringAnimationId
        return skillRecipe("fletching.string.$output", output) {
            material(input); material(1777); requirement(level); experience(experience); animation(animation); delay(2)
            success("You string the bow.")
        }
    }
}
