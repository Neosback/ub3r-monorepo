package net.dodian.uber.game.content.platform

import net.dodian.cache.`object`.GameObjectData
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
        val craftingStandardLeather = SkillDataRegistry.craftingStandardLeatherCrafts()
        val craftingTanning = SkillDataRegistry.craftingTanningDefinitions()
        val craftingGoldJewelry = SkillDataRegistry.craftingGoldJewelryDefinition()
        val craftingFillSources = SkillDataRegistry.craftingResourceFillSources()
        checkDuplicates(craftingHides.map { it.itemId }, "crafting.hide.itemId")
        checkDuplicates(craftingGems.map { it.uncutId }, "crafting.gem.uncutId")
        checkDuplicates(craftingOrbs.map { it.orbId }, "crafting.orb.orbId")
        checkDuplicates(craftingStandardLeather.map { it.productId }, "crafting.standardLeather.productId")
        checkDuplicates(craftingTanning.map { it.hideType }, "crafting.tanning.hideType")
        checkDuplicates(craftingGoldJewelry.groups.map { it.groupId }, "crafting.goldJewelry.groupId")
        checkDuplicates(craftingGoldJewelry.groups.map { it.interfaceSlot }, "crafting.goldJewelry.interfaceSlot")
        checkDuplicates(craftingFillSources.map { it.sourceKey }, "crafting.resourceFillSources.sourceKey")
        checkDuplicates(craftingFillSources.flatMap { it.objectIds }, "crafting.resourceFillSources.objectIds")
        checkDuplicates(
            craftingGoldJewelry.groups.flatMap { it.requiredMouldId.let { mouldId -> listOf(mouldId) } },
            "crafting.goldJewelry.requiredMouldId",
        )
        checkDuplicates(
            craftingGoldJewelry.groups.flatMap { it.jewelryItemIds },
            "crafting.goldJewelry.jewelryItemId",
        )
        check(craftingGoldJewelry.requiredGemItems.isNotEmpty()) { "crafting.goldJewelry.requiredGemItems must not be empty" }
        check(craftingGoldJewelry.strungAmulets.isNotEmpty()) { "crafting.goldJewelry.strungAmulets must not be empty" }
        craftingGoldJewelry.groups.forEach { group ->
            val expectedWidth = craftingGoldJewelry.requiredGemItems.size
            check(group.blankItemIds.size == expectedWidth) {
                "crafting.goldJewelry group ${group.groupId} blankItemIds size must match requiredGemItems size"
            }
            check(group.jewelryItemIds.size == expectedWidth) {
                "crafting.goldJewelry group ${group.groupId} jewelryItemIds size must match requiredGemItems size"
            }
            check(group.requiredLevels.size == expectedWidth) {
                "crafting.goldJewelry group ${group.groupId} requiredLevels size must match requiredGemItems size"
            }
            check(group.experienceByTen.size == expectedWidth) {
                "crafting.goldJewelry group ${group.groupId} experienceByTen size must match requiredGemItems size"
            }
        }
        craftingFillSources.forEach { source ->
            check(source.objectIds.isNotEmpty()) { "crafting resource fill source ${source.sourceKey} must define objectIds" }
            check(source.entries.isNotEmpty()) { "crafting resource fill source ${source.sourceKey} must define entries" }
        }
        validateItemIds(
            craftingHides.flatMap { listOf(it.itemId, it.glovesId, it.chapsId, it.bodyId) } +
                craftingGems.flatMap { listOf(it.uncutId, it.cutId) } +
                craftingOrbs.flatMap { listOf(it.orbId, it.staffId) } +
                craftingStandardLeather.map { it.productId } +
                craftingTanning.flatMap { listOf(it.hideId, it.leatherId) } +
                craftingGoldJewelry.requiredGemItems.filter { it > 0 } +
                craftingGoldJewelry.strungAmulets +
                craftingFillSources.flatMap { source ->
                    source.entries.flatMap { listOf(it.emptyItemId, it.filledItemId) }
                } +
                craftingGoldJewelry.groups.flatMap { group ->
                    group.blankItemIds.filter { it > 0 } +
                        group.jewelryItemIds +
                        listOf(group.requiredMouldId).filter { it > 0 }
                },
            "crafting",
        )
        validateObjectIds(craftingFillSources.flatMap { it.objectIds }, "crafting.resourceFillSources.objects")

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
        check(smithing.smithingLayout.isNotEmpty()) { "smithing.smithingLayout must not be empty" }
        checkDuplicates(
            smithing.smeltingRecipes.flatMap { it.buttonGroups.flatMap { group -> group.rawButtonIds } },
            "smithing.buttonId",
        )
        checkDuplicates(smithing.smeltingRecipes.map { it.barId }, "smithing.smeltingRecipe.barId")
        checkDuplicates(smithing.smithingTiers.map { it.typeId }, "smithing.smithingTier.typeId")
        checkDuplicates(smithing.smithingTiers.map { it.barId }, "smithing.smithingTier.barId")
        smithing.smithingTiers.forEach { tier ->
            check(tier.products.size <= smithing.smithingLayout.size) {
                "smithing tier ${tier.displayName} has ${tier.products.size} products but layout has ${smithing.smithingLayout.size} slots"
            }
        }
        validateItemIds(
            smithing.smeltingRecipes.flatMap { recipe ->
                recipe.oreRequirements.map { it.itemId } + listOf(recipe.barId)
            } + smithing.smithingTiers.flatMap { tier ->
                listOf(tier.barId) + tier.products.map { it.itemId }
            },
            "smithing",
        )
        smithing.smeltingRecipes.forEach { recipe ->
            recipe.buttonGroups.forEach { group ->
                check(group.amount > 0) { "smithing.buttonGroup.amount must be > 0 for barId=${recipe.barId}" }
                check(group.rawButtonIds.isNotEmpty()) { "smithing.buttonGroup.rawButtonIds must not be empty for barId=${recipe.barId}" }
            }
        }

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

        val thieving = SkillDataRegistry.thievingDefinitions()
        val thievingSpecialChests = SkillDataRegistry.thievingSpecialChests()
        val thievingPlunder = SkillDataRegistry.thievingPlunderData()
        check(thieving.isNotEmpty()) { "thieving.definitions must not be empty" }
        checkDuplicates(thieving.map { it.entityId }, "thieving.entityId")
        val thievingStallObjects = SkillDataRegistry.thievingStallObjects().toList()
        val thievingChestObjects = SkillDataRegistry.thievingChestObjects().toList()
        val thievingPlunderObjects = SkillDataRegistry.thievingPlunderObjects().toList()
        checkDuplicates(thievingStallObjects, "thieving.stallObjectId")
        checkDuplicates(thievingChestObjects, "thieving.chestObjectId")
        checkDuplicates(thievingPlunderObjects, "thieving.plunderObjectId")
        checkDuplicates(thievingSpecialChests.map { "${it.objectId}:${it.location.x}:${it.location.y}:${it.location.z}" }, "thieving.specialChest.objectId+position")
        check(thievingPlunder.allDoors.isNotEmpty()) { "thieving.plunder.allDoors must not be empty" }
        check(thievingPlunder.roomEntrances.isNotEmpty()) { "thieving.plunder.roomEntrances must not be empty" }
        check(thievingPlunder.obstacles.isNotEmpty()) { "thieving.plunder.obstacles must not be empty" }
        check(thievingPlunder.urnConfig.isNotEmpty()) { "thieving.plunder.urnConfig must not be empty" }
        check(thievingPlunder.tombConfig.isNotEmpty()) { "thieving.plunder.tombConfig must not be empty" }
        thieving.forEach { definition ->
            check(definition.rewards.isNotEmpty()) { "thieving ${definition.name} must define at least one reward" }
            var previousChance = 0
            definition.rewards.forEach { reward ->
                check(reward.minAmount > 0) { "thieving ${definition.name} reward minAmount must be > 0" }
                check(reward.maxAmount >= reward.minAmount) { "thieving ${definition.name} reward maxAmount must be >= minAmount" }
                check(reward.chance in 1..100) { "thieving ${definition.name} reward chance must be within 1..100" }
                check(reward.chance >= previousChance) { "thieving ${definition.name} reward chance values must be non-decreasing" }
                previousChance = reward.chance
            }
        }
        thievingSpecialChests.forEach { chest ->
            check(chest.requiredLevel >= 1) { "thieving special chest ${chest.chestKey} requiredLevel must be >= 1" }
            check(chest.rareRewardChancePercent in 0.0..100.0) { "thieving special chest ${chest.chestKey} rareRewardChancePercent must be in 0..100" }
            check(chest.coinsMin >= 0) { "thieving special chest ${chest.chestKey} coinsMin must be >= 0" }
            check(chest.coinsMaxOffset >= 0) { "thieving special chest ${chest.chestKey} coinsMaxOffset must be >= 0" }
            check(chest.baseXp >= 0) { "thieving special chest ${chest.chestKey} baseXp must be >= 0" }
            check(chest.randomEventXp >= 0) { "thieving special chest ${chest.chestKey} randomEventXp must be >= 0" }
        }
        validateItemIds(
            thieving.flatMap { definition -> definition.rewards.map { it.itemId } } +
                thievingSpecialChests.flatMap { chest -> chest.rareRewards.map { it.itemId } },
            "thieving",
        )
        validateObjectIds(
            thievingStallObjects + thievingChestObjects + thievingPlunderObjects + thievingSpecialChests.map { it.objectId },
            "thieving.objects",
        )

        val craftingResourceFillObjects = SkillDataRegistry.craftingResourceFillObjects().toList()
        val craftingSpinningWheelObjects = SkillDataRegistry.craftingSpinningWheelObjects().toList()
        val cookingRangeObjects = SkillDataRegistry.cookingRangeObjects().toList()
        val smithingAnvilObjects = SkillDataRegistry.smithingAnvilObjects().toList()
        val smithingFurnaceObjects = SkillDataRegistry.smithingFurnaceObjects().toList()
        val smithingSmeltingInterfaceFurnaces = SkillDataRegistry.smithingSmeltingInterfaceFurnaces().toList()
        val farmingPatchGuideObjects = SkillDataRegistry.farmingPatchGuideObjects().toList()
        val runecraftingAltarObjects = SkillDataRegistry.runecraftingAltarObjects().toList()

        checkDuplicates(craftingResourceFillObjects, "skillObjects.craftingResourceFillObjects")
        checkDuplicates(craftingSpinningWheelObjects, "skillObjects.craftingSpinningWheelObjects")
        checkDuplicates(cookingRangeObjects, "skillObjects.cookingRangeObjects")
        checkDuplicates(smithingAnvilObjects, "skillObjects.smithingAnvilObjects")
        checkDuplicates(smithingFurnaceObjects, "skillObjects.smithingFurnaceObjects")
        checkDuplicates(smithingSmeltingInterfaceFurnaces, "skillObjects.smithingSmeltingInterfaceFurnaces")
        checkDuplicates(farmingPatchGuideObjects, "skillObjects.farmingPatchGuideObjects")
        checkDuplicates(runecraftingAltarObjects, "skillObjects.runecraftingAltarObjects")

        validateObjectIds(
            craftingResourceFillObjects +
                craftingSpinningWheelObjects +
                cookingRangeObjects +
                smithingAnvilObjects +
                smithingFurnaceObjects +
                smithingSmeltingInterfaceFurnaces +
                farmingPatchGuideObjects +
                runecraftingAltarObjects,
            "skillObjects",
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

    private fun validateObjectIds(objectIds: List<Int>, label: String) {
        val invalid = objectIds.filter { it <= 0 }.distinct().sorted()
        if (invalid.isNotEmpty()) {
            throw IllegalStateException("Invalid object ids in $label: ${invalid.joinToString(",")}")
        }
        val missing = objectIds.filter { runCatching { GameObjectData.forId(it) }.getOrNull() == null }.distinct().sorted()
        if (missing.isNotEmpty()) {
            throw IllegalStateException("Unknown object ids in $label: ${missing.joinToString(",")}")
        }
    }
}
