package net.dodian.uber.game.content.commands.dev

import net.dodian.uber.game.content.ContentModuleIndex
import net.dodian.uber.game.content.commands.CommandContent
import net.dodian.uber.game.content.commands.CommandContext
import net.dodian.uber.game.content.commands.commands
import net.dodian.uber.game.content.platform.ContentValidationService
import net.dodian.uber.game.content.platform.InterfaceMappingRegistry
import net.dodian.uber.game.content.platform.MagicDataFile
import net.dodian.uber.game.content.platform.ModuleConfigRegistry
import net.dodian.uber.game.content.platform.PluginModuleMetadata
import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.content.platform.SkillGuideDataFile
import net.dodian.uber.game.content.platform.TravelObjectsDataFile
import net.dodian.uber.game.content.skills.cooking.CookingDefinitions
import net.dodian.uber.game.content.skills.fishing.FishingDefinitions
import net.dodian.uber.game.content.skills.fletching.FletchingDefinitions

object DevContentCommands : CommandContent {
    override fun definitions() =
        commands {
            command("contentdump") {
                handleContentDump(this)
            }
            command("contentvalidate") {
                handleContentValidate(this)
            }
            command("skillpreview") {
                handleSkillPreview(this)
            }
        }
}

private fun handleContentDump(context: CommandContext): Boolean {
    if (!context.specialRights) {
        return false
    }

    val filter = context.parts.getOrNull(1)?.trim()?.lowercase()
    if (filter == null) {
        val moduleMetadata = ModuleConfigRegistry.metadata().values
        val enabledModules = moduleMetadata.count { ModuleConfigRegistry.get(it.moduleKey).enabled }
        context.reply("Content modules: enabled=$enabledModules total=${moduleMetadata.size}")
        context.reply(
            "Loaded counts: cooking=${SkillDataRegistry.cookingRecipes(emptyList()).size}, fishing=${SkillDataRegistry.fishingSpots(emptyList()).size}, fletchingLogs=${SkillDataRegistry.fletchingBowLogs(emptyList()).size}, fletchingArrows=${SkillDataRegistry.fletchingArrowRecipes(emptyList()).size}, fletchingDarts=${SkillDataRegistry.fletchingDartRecipes(emptyList()).size}",
        )
        val magic = InterfaceMappingRegistry.magicData(MagicDataFile())
        val skillGuide = InterfaceMappingRegistry.skillGuideData(SkillGuideDataFile())
        val travel = InterfaceMappingRegistry.travelData(TravelObjectsDataFile())
        context.reply(
            "Mappings: magicTeleports=${magic.teleports.size}, skillGuideButtons=${skillGuide.skillButtons.size}, skillGuideSubTabs=${skillGuide.subTabs.size}, travelPassages=${travel.passageObjects.size}, travelTeleports=${travel.teleportObjects.size}",
        )
        context.reply("Use ::contentdump <moduleKey|skill> for module details.")
        return true
    }

    val skillHandled = dumpSkill(filter, context)
    if (skillHandled) {
        return true
    }

    val module = ModuleConfigRegistry.metadata().values.firstOrNull {
        it.moduleKey.equals(filter, ignoreCase = true) ||
            it.moduleClass.equals(filter, ignoreCase = true) ||
            it.moduleClass.substringAfterLast('.').equals(filter, ignoreCase = true)
    }
    if (module == null) {
        context.reply("No module or skill found for '$filter'.")
        return true
    }

    dumpModule(module, context)
    return true
}

private fun dumpSkill(skill: String, context: CommandContext): Boolean {
    when (skill) {
        "cooking" -> {
            context.reply("skill=cooking path=content/skills/cooking.toml loaded=${SkillDataRegistry.cookingRecipes(emptyList()).size}")
            return true
        }
        "fishing" -> {
            context.reply("skill=fishing path=content/skills/fishing.toml loaded=${SkillDataRegistry.fishingSpots(emptyList()).size}")
            return true
        }
        "fletching" -> {
            context.reply(
                "skill=fletching path=content/skills/fletching.toml loadedLogs=${SkillDataRegistry.fletchingBowLogs(emptyList()).size} loadedArrows=${SkillDataRegistry.fletchingArrowRecipes(emptyList()).size} loadedDarts=${SkillDataRegistry.fletchingDartRecipes(emptyList()).size}",
            )
            return true
        }
        "magic" -> {
            val data = InterfaceMappingRegistry.magicData(MagicDataFile())
            context.reply("skill=magic path=content/interfaces/magic.toml loadedTeleports=${data.teleports.size}")
            return true
        }
        "skillguide" -> {
            val data = InterfaceMappingRegistry.skillGuideData(SkillGuideDataFile())
            context.reply(
                "skill=skillguide path=content/interfaces/skillguide.toml loadedButtons=${data.skillButtons.size} loadedSubTabs=${data.subTabs.size}",
            )
            return true
        }
        "travel" -> {
            val data = InterfaceMappingRegistry.travelData(TravelObjectsDataFile())
            context.reply(
                "skill=travel path=content/objects/travel.toml loadedPassages=${data.passageObjects.size} loadedTeleports=${data.teleportObjects.size} loadedWebs=${data.webObstacleObjects.size}",
            )
            return true
        }
    }
    return false
}

private fun dumpModule(module: PluginModuleMetadata, context: CommandContext) {
    val config = ModuleConfigRegistry.get(module.moduleKey)
    val resolvedDataPath = config.dataPath ?: "content/${module.dataNamespace}"
    val loadedCount = moduleLoadedCount(module)
    context.reply(
        "moduleKey=${module.moduleKey} type=${module.moduleType} enabled=${config.enabled} debug=${config.debug} xpMultiplier=${config.xpMultiplier}",
    )
    context.reply(
        "configPath=${module.configPath} resolvedDataPath=$resolvedDataPath loadedCount=$loadedCount",
    )
}

private fun moduleLoadedCount(module: PluginModuleMetadata): Int {
    val className = module.moduleClass
    return when (module.moduleType) {
        "interfaceButton" -> ContentModuleIndex.interfaceButtons.count { it::class.java.name == className }
        "objectContent" -> ContentModuleIndex.objectContents.count { it.second::class.java.name == className }
        "itemContent" -> ContentModuleIndex.itemContents.count { it::class.java.name == className }
        "npcContent" -> net.dodian.uber.game.plugin.GeneratedPluginModuleIndex.npcContentModules.count { it.first == module.moduleKey && ModuleConfigRegistry.get(it.first).enabled }
        "eventBootstrap" -> net.dodian.uber.game.plugin.GeneratedPluginModuleIndex.eventBootstrapModules.count { it.first == module.moduleKey && ModuleConfigRegistry.get(it.first).enabled }
        else -> if (ModuleConfigRegistry.get(module.moduleKey).enabled) 1 else 0
    }
}

private fun handleContentValidate(context: CommandContext): Boolean {
    if (!context.specialRights) {
        return false
    }
    return try {
        ContentValidationService.validate()
        context.reply("Content validation passed.")
        true
    } catch (e: Exception) {
        context.reply("Content validation failed: ${e.message}")
        true
    }
}

private fun handleSkillPreview(context: CommandContext): Boolean {
    if (!context.specialRights) {
        return false
    }
    val skill = context.parts.getOrNull(1)?.lowercase()
        ?: return context.usage("Usage: ::skillpreview <cooking|fishing|fletching> ...")

    return when (skill) {
        "cooking" -> previewCooking(context)
        "fishing" -> previewFishing(context)
        "fletching" -> previewFletching(context)
        else -> context.usage("Unsupported skill '$skill'. Try cooking, fishing, or fletching.")
    }
}

private fun previewCooking(context: CommandContext): Boolean {
    if (context.parts.size < 4) {
        return context.usage("Usage: ::skillpreview cooking <rawItemId> <cookingLevel> [gauntlets=0|1] [chefHat=0|1]")
    }
    val rawItemId = context.parts[2].toIntOrNull() ?: return context.usage("Invalid rawItemId.")
    val level = context.parts[3].toIntOrNull() ?: return context.usage("Invalid cookingLevel.")
    val gauntlets = context.parts.getOrNull(4)?.toIntOrNull() == 1
    val chefHat = context.parts.getOrNull(5)?.toIntOrNull() == 1
    val recipe = CookingDefinitions.findRecipe(rawItemId)
        ?: return context.usage("No cooking recipe for raw item $rawItemId")

    var burnRoll = recipe.burnRollBase - level
    if (gauntlets) burnRoll -= 4
    if (chefHat) burnRoll -= 4
    if (gauntlets && chefHat) burnRoll -= 2
    val clampedBurnRoll = burnRoll.coerceIn(0, 100)
    val burnChance = clampedBurnRoll / 100.0
    val successChance = 1.0 - burnChance

    context.reply(
        "cooking raw=${recipe.rawItemId} req=${recipe.requiredLevel} xp=${recipe.experience} burnChance=${"%.2f".format(burnChance * 100)}% successChance=${"%.2f".format(successChance * 100)}%",
    )
    return true
}

private fun previewFishing(context: CommandContext): Boolean {
    if (context.parts.size < 4) {
        return context.usage("Usage: ::skillpreview fishing <spotIndex> <fishingLevel> [dragonHarpoon=0|1]")
    }
    val spotIndex = context.parts[2].toIntOrNull() ?: return context.usage("Invalid spotIndex.")
    val level = context.parts[3].toIntOrNull() ?: return context.usage("Invalid fishingLevel.")
    val dragonHarpoon = context.parts.getOrNull(4)?.toIntOrNull() == 1
    val spot = FishingDefinitions.byIndex(spotIndex)
        ?: return context.usage("No fishing spot for index $spotIndex")

    val bonus = 1.0 + (level / 256.0) + if (dragonHarpoon && level >= 61) 0.2 else 0.0
    val delayMs = (spot.baseDelayMs / bonus).toLong().coerceAtLeast(1L)

    context.reply(
        "fishing spot=${spot.index} fish=${spot.fishItemId} req=${spot.requiredLevel} xp=${spot.experience} delayMs~$delayMs feathers=${spot.featherConsumed} premium=${spot.premiumOnly}",
    )
    return true
}

private fun previewFletching(context: CommandContext): Boolean {
    if (context.parts.size < 4) {
        return context.usage("Usage: ::skillpreview fletching <bowlog|arrow|dart> <id>")
    }
    val mode = context.parts[2].lowercase()
    val id = context.parts[3].toIntOrNull() ?: return context.usage("Invalid id.")

    when (mode) {
        "bowlog" -> {
            val def = FletchingDefinitions.findBowLogByLog(id)
                ?: return context.usage("No bow log definition for log item $id")
            context.reply(
                "fletching bowlog=${def.logItemId} short(req=${def.shortLevelRequired},xp=${def.shortExperience}) long(req=${def.longLevelRequired},xp=${def.longExperience})",
            )
            return true
        }
        "arrow" -> {
            val def = FletchingDefinitions.findArrowRecipeByHead(id)
                ?: return context.usage("No arrow definition for head $id")
            context.reply("fletching arrow head=${def.headId} output=${def.arrowId} req=${def.requiredLevel} xp=${def.experience}")
            return true
        }
        "dart" -> {
            val def = FletchingDefinitions.findDartRecipeByTip(id)
                ?: return context.usage("No dart definition for tip $id")
            context.reply("fletching dart tip=${def.tipId} output=${def.dartId} req=${def.requiredLevel} xp=${def.experience}")
            return true
        }
        else -> return context.usage("Unknown fletching mode '$mode'. Use bowlog, arrow, or dart.")
    }
}
