package net.dodian.uber.game.content.platform

import net.dodian.uber.game.Server
import net.dodian.uber.game.content.ContentModuleIndex

object ContentPlatformBootstrap {
    @JvmStatic
    fun bootstrapAndValidate() {
        ModuleConfigRegistry.bootstrap(ContentModuleIndex.moduleMetadata)
        ContentValidationService.validate()
    }
}

object ContentValidationService {
    @JvmStatic
    fun validate() {
        val cooking = SkillDataRegistry.cookingRecipes()
        checkDuplicates(cooking.map { it.rawItemId }, "cooking.rawItemId")
        validateItemIds(cooking.flatMap { listOf(it.rawItemId, it.cookedItemId, it.burntItemId) }, "cooking")

        val fishing = SkillDataRegistry.fishingSpots()
        checkDuplicates(fishing.map { "${it.objectId}:${it.clickType}" }, "fishing.objectId+clickType")
        checkDuplicates(fishing.map { it.index }, "fishing.index")
        validateItemIds(fishing.map { it.fishItemId } + fishing.map { it.toolItemId }, "fishing")

        val fletchBow = SkillDataRegistry.fletchingBowLogs()
        val fletchArrow = SkillDataRegistry.fletchingArrowRecipes()
        val fletchDart = SkillDataRegistry.fletchingDartRecipes()
        checkDuplicates(fletchBow.map { it.logItemId }, "fletching.logItemId")
        checkDuplicates(fletchArrow.map { it.headId }, "fletching.arrowHeadId")
        checkDuplicates(fletchDart.map { it.tipId }, "fletching.dartTipId")
        validateItemIds(
            fletchBow.flatMap {
                listOf(
                    it.logItemId,
                    it.unstrungShortbowId,
                    it.unstrungLongbowId,
                    it.shortbowId,
                    it.longbowId,
                )
            } +
                fletchArrow.flatMap { listOf(it.headId, it.arrowId) } +
                fletchDart.flatMap { listOf(it.tipId, it.dartId) },
            "fletching",
        )

        val woodcuttingTrees = SkillDataRegistry.woodcuttingTrees()
        val woodcuttingAxes = SkillDataRegistry.woodcuttingAxes()
        checkDuplicates(woodcuttingTrees.flatMap { it.objectIds.toList() }, "woodcutting.objectIds")
        checkDuplicates(woodcuttingAxes.map { it.itemId }, "woodcutting.axeItemId")
        validateItemIds(woodcuttingTrees.map { it.logItemId } + woodcuttingAxes.map { it.itemId }, "woodcutting")

        val miningRocks = SkillDataRegistry.miningRocks()
        val miningPickaxes = SkillDataRegistry.miningPickaxes()
        checkDuplicates(miningRocks.flatMap { it.objectIds.toList() }, "mining.objectIds")
        checkDuplicates(miningPickaxes.map { it.itemId }, "mining.pickaxeItemId")
        validateItemIds(miningRocks.map { it.oreItemId } + miningPickaxes.map { it.itemId }, "mining")

        val craftingHides = SkillDataRegistry.craftingHideDefinitions()
        val craftingGems = SkillDataRegistry.craftingGemDefinitions()
        val craftingOrbs = SkillDataRegistry.craftingOrbDefinitions()
        checkDuplicates(craftingHides.map { it.itemId }, "crafting.hide.itemId")
        checkDuplicates(craftingGems.map { it.uncutId }, "crafting.gem.uncutId")
        checkDuplicates(craftingOrbs.map { it.orbId }, "crafting.orb.orbId")
        validateItemIds(
            craftingHides.flatMap { listOf(it.itemId, it.glovesId, it.chapsId, it.bodyId) } +
                craftingGems.flatMap { listOf(it.uncutId, it.cutId) } +
                craftingOrbs.flatMap { listOf(it.orbId, it.staffId) },
            "crafting",
        )

        val herbloreHerbs = SkillDataRegistry.herbloreHerbDefinitions()
        val herbloreRecipes = SkillDataRegistry.herblorePotionRecipes()
        val herbloreDoses = SkillDataRegistry.herblorePotionDoseDefinitions()
        checkDuplicates(herbloreHerbs.map { it.grimyId }, "herblore.grimyId")
        checkDuplicates(herbloreRecipes.map { "${it.unfinishedPotionId}:${it.secondaryId}" }, "herblore.recipe.input")
        checkDuplicates(herbloreDoses.map { it.fourDoseId }, "herblore.fourDoseId")
        validateItemIds(
            herbloreHerbs.flatMap { listOf(it.grimyId, it.cleanId, it.unfinishedPotionId) } +
                herbloreRecipes.flatMap { listOf(it.unfinishedPotionId, it.secondaryId, it.finishedPotionId) } +
                herbloreDoses.flatMap { listOf(it.oneDoseId, it.twoDoseId, it.threeDoseId, it.fourDoseId) },
            "herblore",
        )

        val runecraftingAltars = SkillDataRegistry.runecraftingAltars()
        checkDuplicates(runecraftingAltars.map { it.objectId }, "runecrafting.objectId")
        validateItemIds(
            listOf(SkillDataRegistry.runecraftingRuneEssenceId()) + runecraftingAltars.map { it.request.runeId },
            "runecrafting",
        )

        check(SkillDataRegistry.slayerMazchnaTasks().isNotEmpty()) { "slayer.mazchna must not be empty" }
        check(SkillDataRegistry.slayerVannakaTasks().isNotEmpty()) { "slayer.vannaka must not be empty" }
        check(SkillDataRegistry.slayerDuradelTasks().isNotEmpty()) { "slayer.duradel must not be empty" }

        val prayerBones = SkillDataRegistry.prayerBones()
        checkDuplicates(prayerBones.map { it.itemId }, "prayer.bone.itemId")
        validateItemIds(prayerBones.map { it.itemId }, "prayer")
        val prayerAltars = SkillDataRegistry.prayerAltarObjectIds()
        checkDuplicates(prayerAltars.toList(), "prayer.altarObjectId")

        val magic = InterfaceMappingRegistry.magicData()
        checkDuplicates(
            magic.teleports.flatMap { it.rawButtonIds.toList() },
            "magic.teleport.rawButtonIds",
        )
        checkDuplicates(magic.teleports.map { it.componentId }, "magic.teleport.componentId")

        val skillGuide = InterfaceMappingRegistry.skillGuideData()
        checkDuplicates(skillGuide.skillButtons.map { it.skillId }, "skillguide.skillId")
        checkDuplicates(
            skillGuide.skillButtons.flatMap { it.rawButtonIds.toList() } + skillGuide.subTabs.flatMap { it.rawButtonIds.toList() },
            "skillguide.rawButtonIds",
        )

        val travel = InterfaceMappingRegistry.travelData()
        checkDuplicates(travel.passageObjects.toList(), "travel.passageObjects")
        checkDuplicates(travel.teleportObjects.toList(), "travel.teleportObjects")
        checkDuplicates(travel.webObstacleObjects.toList(), "travel.webObstacleObjects")

        val smithing = SkillDataRegistry.smithingData()
        check(smithing.smeltingRecipes.isNotEmpty()) { "smithing.smeltingRecipes must not be empty" }
        check(smithing.smithingTiers.isNotEmpty()) { "smithing.smithingTiers must not be empty" }
        checkDuplicates(smithing.smeltingButtonMappings.map { it.buttonId }, "smithing.buttonId")
        checkDuplicates(smithing.smeltingRecipes.map { it.barId }, "smithing.smeltingRecipe.barId")
        checkDuplicates(smithing.smithingTiers.map { it.typeId }, "smithing.smithingTier.typeId")
        checkDuplicates(smithing.smithingTiers.map { it.barId }, "smithing.smithingTier.barId")
        validateItemIds(
            smithing.smeltingRecipes.flatMap { recipe ->
                recipe.oreRequirements.map { it.itemId } + listOf(recipe.barId)
            } + smithing.smithingTiers.flatMap { tier ->
                listOf(tier.barId) + tier.products.map { it.itemId }
            },
            "smithing",
        )

        val farming = SkillDataRegistry.farmingData()
        check(farming.patchDefinitions.isNotEmpty()) { "farming.patchDefinitions must not be empty" }
        check(farming.saplings.isNotEmpty()) { "farming.saplings must not be empty" }
        check(farming.patchGroups.isNotEmpty()) { "farming.patchGroups must not be empty" }
        check(farming.compostTypes.isNotEmpty()) { "farming.compostTypes must not be empty" }
        check(farming.compostBins.isNotEmpty()) { "farming.compostBins must not be empty" }
        checkDuplicates(farming.patchDefinitions.map { "${it.family}:${it.seed}" }, "farming.seedByFamily")
        checkDuplicates(farming.patchGroups.flatMap { it.objectIds }, "farming.patchGroup.objectId")
        checkDuplicates(farming.compostBins.map { it.objectId }, "farming.compostBin.objectId")
        validateItemIds(
            farming.patchDefinitions.map { it.seed } +
                farming.patchDefinitions.map { it.harvestItem }.filter { it > 0 } +
                farming.saplings.flatMap { listOf(it.treeSeed, it.plantedId, it.waterId, it.saplingId) } +
                farming.compostTypes.map { it.itemId }.filter { it > 0 } +
                farming.regularCompostItems +
                farming.superCompostItems +
                listOf(
                    farming.BUCKET,
                    farming.SPADE,
                    farming.RAKE,
                    farming.SEED_DIBBER,
                    farming.TROWEL,
                    farming.FILLED_PLANT_POT,
                    farming.EMPTY_PLANT_POT,
                    farming.SECATEURS,
                    farming.MAGIC_SECATEURS,
                    farming.PLANT_CURE,
                    farming.VOLCANIC_ASH,
                ),
            "farming",
        )
    }

    private fun checkDuplicates(values: List<Any>, label: String) {
        val duplicates = values.groupBy { it }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw IllegalStateException("Duplicate values in $label: ${duplicates.joinToString(",")}")
        }
    }

    private fun validateItemIds(itemIds: List<Int>, label: String) {
        val itemManager = Server.itemManager ?: return
        val missing = itemIds.filter { !itemManager.items.containsKey(it) }.distinct().sorted()
        if (missing.isNotEmpty()) {
            throw IllegalStateException("Unknown item ids in $label: ${missing.joinToString(",")}")
        }
    }
}
