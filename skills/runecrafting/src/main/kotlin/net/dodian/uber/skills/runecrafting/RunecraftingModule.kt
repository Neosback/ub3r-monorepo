package net.dodian.uber.skills.runecrafting

import net.dodian.uber.game.api.content.ContentAttributeKey
import net.dodian.uber.game.api.plugin.ContentMaturity
import net.dodian.uber.game.api.plugin.ContentModuleManifest
import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.api.plugin.skills.SkillPluginDefinition
import net.dodian.uber.game.api.plugin.skills.manifest
import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.api.SkillModuleDescriptor
import net.dodian.uber.skills.runtime.TomlRecordReader

data class RunecraftingAltarDef(
    val objectId: Int,
    val runeId: Int,
    val requiredLevel: Int,
    val experiencePerEssence: Int,
)

/**
 * Plugin-owned reference implementation for altar rune-crafting. Rune pouches
 * (fill/empty/check) stay in game-server: they're wired straight from raw item-click
 * packet listeners rather than the skill plugin registry, and key off Player-scoped
 * pouch arrays that aren't part of the portable [SkillPlayer] surface.
 */
object RunecraftingModule : SkillPlugin {
    val descriptor = SkillModuleDescriptor("skill.runecrafting", "Runecrafting")

    const val RUNE_ESSENCE_ID = 1436

    /** Last altar-craft timestamp; read by game-server's legacy pouch-fill anti-cheat check. */
    val LAST_ALTAR_CRAFT_KEY: ContentAttributeKey<Long> = ContentAttributeKey("skill.runecrafting", "lastAltarCraftAtMillis")

    val altars: List<RunecraftingAltarDef> by lazy { loadAltars() }

    override val definition: SkillPluginDefinition = skillPlugin("Runecrafting", Skill.RUNECRAFTING) {
        altars.forEach { altar ->
            objectClick(PolicyPreset.GATHERING, option = 1, altar.objectId) { interaction ->
                craftRunes(interaction.player, altar)
            }
        }
    }

    override val contentManifest: ContentModuleManifest = definition.manifest(
        id = descriptor.id,
        owner = "gameplay",
        version = descriptor.version,
        maturity = ContentMaturity.STABLE,
    )

    private fun craftRunes(player: SkillPlayer, altar: RunecraftingAltarDef): Boolean {
        val essenceCount = player.inventory.amount(RUNE_ESSENCE_ID)
        if (essenceCount <= 0) {
            player.ui.message("You do not have any rune essence!")
            return false
        }
        if (player.skills.current(Skill.RUNECRAFTING) < altar.requiredLevel) {
            val runeName = player.inventory.itemName(altar.runeId).lowercase()
            player.ui.message("You must have ${altar.requiredLevel} runecrafting to craft $runeName")
            return false
        }

        val bonusChance = ((player.skills.current(Skill.RUNECRAFTING) + 1) / 2).coerceIn(0, 100)
        var extra = 0
        repeat(essenceCount) {
            player.inventory.remove(RUNE_ESSENCE_ID, 1)
            if (player.random.chance(bonusChance, 100)) extra++
        }

        val crafted = essenceCount + extra
        val runeName = player.inventory.itemName(altar.runeId).lowercase()
        player.ui.message("You craft $crafted ${runeName}s")
        player.inventory.add(altar.runeId, crafted)
        player.inventory.refresh()
        val xp = altar.experiencePerEssence * essenceCount
        player.skills.gainXp(xp, Skill.RUNECRAFTING)
        player.actions.triggerRandomEvent(xp)
        player.attributes.put(LAST_ALTAR_CRAFT_KEY, System.currentTimeMillis())
        return true
    }

    private fun loadAltars(): List<RunecraftingAltarDef> =
        TomlRecordReader.readRecords("runecrafting/altars.toml", "altar").map { row ->
            RunecraftingAltarDef(
                objectId = row.getValue("objectId").toInt(),
                runeId = row.getValue("runeId").toInt(),
                requiredLevel = row.getValue("requiredLevel").toInt(),
                experiencePerEssence = row.getValue("experiencePerEssence").toInt(),
            )
        }
}
