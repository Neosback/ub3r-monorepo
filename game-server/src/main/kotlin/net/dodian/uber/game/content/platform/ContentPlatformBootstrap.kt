package net.dodian.uber.game.content.platform

import net.dodian.uber.game.world.cache.`object`.GameObjectData
import net.dodian.uber.game.Server
import net.dodian.uber.game.content.ContentModuleIndex
import net.dodian.uber.game.content.skills.guide.SkillGuideDataRegistry
import org.slf4j.LoggerFactory

object ContentPlatformBootstrap {
    @JvmStatic
    fun bootstrapAndValidate() {
        ModuleConfigRegistry.bootstrap(ContentModuleIndex.moduleMetadata)
        ContentValidationService.validate()
    }
}

object ContentValidationService {
    private val logger = LoggerFactory.getLogger(ContentValidationService::class.java)

    private enum class ValidationSeverity {
        ERROR,
        WARN,
    }

    @JvmStatic
    fun validate() {
        val domainCounts = linkedMapOf<String, Int>()
        val warnings = mutableListOf<String>()

        val cooking = SkillDataRegistry.cookingRecipes()
        domainCounts["skills.cooking.recipes"] = cooking.size
        checkDuplicates(cooking.map { it.rawItemId }, "cooking.rawItemId")
        validateItemIds(cooking.flatMap { listOf(it.rawItemId, it.cookedItemId, it.burntItemId) }, "cooking")

        val fishing = SkillDataRegistry.fishingSpots()
        domainCounts["skills.fishing.spots"] = fishing.size
        checkDuplicates(fishing.map { "${it.objectId}:${it.clickType}" }, "fishing.objectId+clickType")
        checkDuplicates(fishing.map { it.index }, "fishing.index")
        validateItemIds(fishing.map { it.fishItemId } + fishing.map { it.toolItemId }, "fishing")

        val fletchBow = SkillDataRegistry.fletchingBowLogs()
        val fletchArrow = SkillDataRegistry.fletchingArrowRecipes()
        val fletchDart = SkillDataRegistry.fletchingDartRecipes()
        domainCounts["skills.fletching.logs"] = fletchBow.size
        domainCounts["skills.fletching.arrows"] = fletchArrow.size
        domainCounts["skills.fletching.darts"] = fletchDart.size
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
        domainCounts["skills.woodcutting.trees"] = woodcuttingTrees.size
        domainCounts["skills.woodcutting.axes"] = woodcuttingAxes.size
        checkDuplicates(woodcuttingTrees.flatMap { it.objectIds.toList() }, "woodcutting.objectIds")
        checkDuplicates(woodcuttingAxes.map { it.itemId }, "woodcutting.axeItemId")
        validateItemIds(woodcuttingTrees.map { it.logItemId } + woodcuttingAxes.map { it.itemId }, "woodcutting")

        val miningRocks = SkillDataRegistry.miningRocks()
        val miningPickaxes = SkillDataRegistry.miningPickaxes()
        domainCounts["skills.mining.rocks"] = miningRocks.size
        domainCounts["skills.mining.pickaxes"] = miningPickaxes.size
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
        domainCounts["skills.crafting.hides"] = craftingHides.size
        domainCounts["skills.crafting.gems"] = craftingGems.size
        domainCounts["skills.crafting.orbs"] = craftingOrbs.size
        domainCounts["skills.crafting.tanning"] = craftingTanning.size
        domainCounts["skills.crafting.fillSources"] = craftingFillSources.size
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
        val tanningHeaderStrings = SkillDataRegistry.craftingTanningHeaderStrings()
        val tanningLabelIds = SkillDataRegistry.craftingTanningHigherTierLabelIds().toList()
        val tanningModels = SkillDataRegistry.craftingTanningInterfaceModels()
        check(tanningHeaderStrings.isNotEmpty()) { "crafting.tanningHeaderStrings must not be empty" }
        check(tanningLabelIds.isNotEmpty() && tanningLabelIds.size % 2 == 0) { "crafting.tanningHigherTierLabelIds must have even non-zero size" }
        check(tanningModels.isNotEmpty()) { "crafting.tanningInterfaceModels must not be empty" }
        checkDuplicates(tanningHeaderStrings.map { it.componentId }, "crafting.tanningHeaderStrings.componentId")
        checkDuplicates(tanningLabelIds, "crafting.tanningHigherTierLabelIds")
        checkDuplicates(tanningModels.map { it.componentId }, "crafting.tanningInterfaceModels.componentId")
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
        domainCounts["skills.herblore.herbs"] = herbloreHerbs.size
        domainCounts["skills.herblore.recipes"] = herbloreRecipes.size
        domainCounts["skills.herblore.doses"] = herbloreDoses.size
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
        domainCounts["skills.runecrafting.altars"] = runecraftingAltars.size
        checkDuplicates(runecraftingAltars.map { it.objectId }, "runecrafting.objectId")
        validateItemIds(
            listOf(SkillDataRegistry.runecraftingRuneEssenceId()) + runecraftingAltars.map { it.request.runeId },
            "runecrafting",
        )

        check(SkillDataRegistry.slayerMazchnaTasks().isNotEmpty()) { "slayer.mazchna must not be empty" }
        check(SkillDataRegistry.slayerVannakaTasks().isNotEmpty()) { "slayer.vannaka must not be empty" }
        check(SkillDataRegistry.slayerDuradelTasks().isNotEmpty()) { "slayer.duradel must not be empty" }

        val prayerBones = SkillDataRegistry.prayerBones()
        domainCounts["skills.prayer.bones"] = prayerBones.size
        checkDuplicates(prayerBones.map { it.itemId }, "prayer.bone.itemId")
        validateItemIds(prayerBones.map { it.itemId }, "prayer")
        val prayerAltars = SkillDataRegistry.prayerAltarObjectIds()
        checkDuplicates(prayerAltars.toList(), "prayer.altarObjectId")

        val magic = InterfaceMappingRegistry.magicData()
        domainCounts["interfaces.magic.teleports"] = magic.teleports.size
        checkDuplicates(
            magic.teleports.flatMap { it.rawButtonIds.toList() },
            "magic.teleport.rawButtonIds",
        )
        checkDuplicates(magic.teleports.map { it.componentId }, "magic.teleport.componentId")

        val skillGuide = InterfaceMappingRegistry.skillGuideData()
        domainCounts["interfaces.skillguide.skillButtons"] = skillGuide.skillButtons.size
        domainCounts["interfaces.skillguide.subTabs"] = skillGuide.subTabs.size
        checkDuplicates(skillGuide.skillButtons.map { it.skillId }, "skillguide.skillId")
        checkDuplicates(
            skillGuide.skillButtons.flatMap { it.rawButtonIds.toList() } + skillGuide.subTabs.flatMap { it.rawButtonIds.toList() },
            "skillguide.rawButtonIds",
        )
        checkDuplicates(skillGuide.baselineHidden.toList(), "skillguide.baselineHidden")
        checkDuplicates(skillGuide.baselineShown.toList(), "skillguide.baselineShown")
        checkDuplicates(skillGuide.titleComponentIds.toList(), "skillguide.titleComponentIds")

        val travel = InterfaceMappingRegistry.travelData()
        domainCounts["objects.travel.total"] = travel.passageObjects.size + travel.teleportObjects.size + travel.webObstacleObjects.size
        checkDuplicates(travel.passageObjects.toList(), "travel.passageObjects")
        checkDuplicates(travel.teleportObjects.toList(), "travel.teleportObjects")
        checkDuplicates(travel.webObstacleObjects.toList(), "travel.webObstacleObjects")

        val ui = InterfaceMappingRegistry.uiData()
        domainCounts["interfaces.ui.totalButtons"] =
            ui.runOffButtons.size +
                ui.runOnButtons.size +
                ui.runToggleButtons.size +
                ui.tabInterfaceDefaultButtons.size +
                ui.tabInterfaceEquipmentButtons.size +
                ui.sidebarHomeButtons.size +
                ui.closeInterfaceButtons.size +
                ui.questTabToggleButtons.size +
                ui.logoutButtons.size +
                ui.morphButtons.size
        checkDuplicates(
            ui.runOffButtons.toList() +
                ui.runOnButtons.toList() +
                ui.runToggleButtons.toList() +
                ui.tabInterfaceDefaultButtons.toList() +
                ui.tabInterfaceEquipmentButtons.toList() +
                ui.sidebarHomeButtons.toList() +
                ui.closeInterfaceButtons.toList() +
                ui.questTabToggleButtons.toList() +
                ui.logoutButtons.toList() +
                ui.morphButtons.toList(),
            "ui.buttonIds",
        )

        val dialogue = InterfaceMappingRegistry.dialogueData()
        domainCounts["interfaces.dialogue.totalButtons"] =
            dialogue.optionOne.size +
                dialogue.optionTwo.size +
                dialogue.optionThree.size +
                dialogue.optionFour.size +
                dialogue.optionFive.size +
                dialogue.toggleSpecialsButtons.size +
                dialogue.toggleBossYellButtons.size
        checkDuplicates(
            dialogue.optionOne.toList() +
                dialogue.optionTwo.toList() +
                dialogue.optionThree.toList() +
                dialogue.optionFour.toList() +
                dialogue.optionFive.toList() +
                dialogue.toggleSpecialsButtons.toList() +
                dialogue.toggleBossYellButtons.toList(),
            "dialogue.buttonIds",
        )

        val bank = InterfaceMappingRegistry.bankData()
        domainCounts["interfaces.bank.totalButtons"] =
            bank.depositInventoryButtons.size +
                bank.depositWornItemsButtons.size +
                bank.withdrawAsNoteButtons.size +
                bank.withdrawAsItemButtons.size +
                bank.searchButtons.size +
                bank.tabButtons.size
        checkDuplicates(
            bank.depositInventoryButtons.toList() +
                bank.depositWornItemsButtons.toList() +
                bank.withdrawAsNoteButtons.toList() +
                bank.withdrawAsItemButtons.toList() +
                bank.searchButtons.toList(),
            "bank.buttonIds",
        )
        checkDuplicates(bank.tabButtons.toList(), "bank.tabButtons")

        val settings = InterfaceMappingRegistry.settingsData()
        domainCounts["interfaces.settings.totalButtons"] =
            settings.openMoreSettingsButtons.size +
                settings.closeMoreSettingsButtons.size +
                settings.pinHelpButtons.size +
                settings.bossYellEnableButtons.size +
                settings.bossYellDisableButtons.size
        checkDuplicates(
            settings.openMoreSettingsButtons.toList() +
                settings.closeMoreSettingsButtons.toList() +
                settings.pinHelpButtons.toList() +
                settings.bossYellEnableButtons.toList() +
                settings.bossYellDisableButtons.toList(),
            "settings.buttonIds",
        )

        val emotes = InterfaceMappingRegistry.emoteData()
        domainCounts["interfaces.emotes.totalButtons"] =
            emotes.goblinBowButtons.size +
                emotes.goblinSaluteButtons.size +
                emotes.glassBoxButtons.size +
                emotes.climbRopeButtons.size +
                emotes.leanButtons.size +
                emotes.glassWallButtons.size +
                emotes.ideaButtons.size +
                emotes.stompButtons.size +
                emotes.skillcapeButtons.size
        checkDuplicates(
            emotes.goblinBowButtons.toList() +
                emotes.goblinSaluteButtons.toList() +
                emotes.glassBoxButtons.toList() +
                emotes.climbRopeButtons.toList() +
                emotes.leanButtons.toList() +
                emotes.glassWallButtons.toList() +
                emotes.ideaButtons.toList() +
                emotes.stompButtons.toList() +
                emotes.skillcapeButtons.toList(),
            "emotes.buttonIds",
        )

        val duel = InterfaceMappingRegistry.duelData()
        domainCounts["interfaces.duel.totalButtons"] = duel.offerRuleButtons.size + duel.bodyRuleButtons.size
        checkDuplicates(duel.offerRuleButtons.toList(), "duel.offerRuleButtons")
        checkDuplicates(duel.bodyRuleButtons.toList(), "duel.bodyRuleButtons")
        checkDuplicates(duel.offerRuleIndices.map { it.buttonId }, "duel.offerRuleIndices.buttonId")

        checkDuplicates(InterfaceMappingRegistry.partyRoomData().depositAcceptButtons.toList(), "partyroom.depositAcceptButtons")
        checkDuplicates(InterfaceMappingRegistry.slotsData().spinButtons.toList(), "slots.spinButtons")
        checkDuplicates(InterfaceMappingRegistry.appearanceData().confirmButtons.toList(), "appearance.confirmButtons")
        checkDuplicates(InterfaceMappingRegistry.rewardData().skillSelectionButtons.toList(), "rewards.skillSelectionButtons")

        val bankingObjects = InterfaceMappingRegistry.bankingObjectsData()
        checkDuplicates(bankingObjects.boothObjects.toList(), "objects.banking.boothObjects")
        checkDuplicates(bankingObjects.chestObjects.toList(), "objects.banking.chestObjects")

        val partyroomObjects = InterfaceMappingRegistry.partyRoomObjectsData()
        domainCounts["objects.partyroom.total"] = partyroomObjects.balloonObjects.size + 2
        checkDuplicates(partyroomObjects.balloonObjects.toList(), "objects.partyroom.balloonObjects")
        check(partyroomObjects.depositChest > 0) { "objects.partyroom.depositChest must be > 0" }
        check(partyroomObjects.forceTrigger > 0) { "objects.partyroom.forceTrigger must be > 0" }

        val smithing = SkillDataRegistry.smithingData()
        domainCounts["skills.smithing.smeltingRecipes"] = smithing.smeltingRecipes.size
        domainCounts["skills.smithing.tiers"] = smithing.smithingTiers.size
        domainCounts["skills.smithing.layoutSlots"] = smithing.smithingLayout.size
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
        val smithingKnownInvalidItemIds = setOf(64182)
        validateItemIds(
            (
                smithing.smeltingRecipes.flatMap { recipe ->
                    recipe.oreRequirements.map { it.itemId } + listOf(recipe.barId)
                } + smithing.smithingTiers.flatMap { tier ->
                    listOf(tier.barId) + tier.products.map { it.itemId }
                }
            ).filterNot { it in smithingKnownInvalidItemIds },
            "smithing",
        )
        smithing.smeltingRecipes.forEach { recipe ->
            recipe.buttonGroups.forEach { group ->
                check(group.amount > 0) { "smithing.buttonGroup.amount must be > 0 for barId=${recipe.barId}" }
                check(group.rawButtonIds.isNotEmpty()) { "smithing.buttonGroup.rawButtonIds must not be empty for barId=${recipe.barId}" }
            }
        }

        val farming = SkillDataRegistry.farmingData()
        domainCounts["skills.farming.patchDefinitions"] = farming.patchDefinitions.size
        domainCounts["skills.farming.patchGroups"] = farming.patchGroups.size
        domainCounts["skills.farming.saplings"] = farming.saplings.size
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
                    ).filter { it > 0 },
            "farming",
        )

        val thieving = SkillDataRegistry.thievingDefinitions()
        val thievingSpecialChests = SkillDataRegistry.thievingSpecialChests()
        val thievingPlunder = SkillDataRegistry.thievingPlunderData()
        domainCounts["skills.thieving.definitions"] = thieving.size
        domainCounts["skills.thieving.specialChests"] = thievingSpecialChests.size
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

        val guideFiles = SkillGuideDataRegistry.all().values
        domainCounts["skills.guides.files"] = guideFiles.size
        check(guideFiles.isNotEmpty()) { "skill guides must not be empty" }
        checkDuplicates(guideFiles.map { it.skillId }, "skillguides.skillId")
        guideFiles.forEach { guide ->
            val guideKey = SkillGuideDataRegistry.keyForSkillId(guide.skillId) ?: "skill-${guide.skillId}"
            check(guide.labels.isNotEmpty()) { "skill guide ${guide.skillId} must define labels" }
            checkDuplicates(guide.labels.map { it.componentId }, "skillguides.${guide.skillId}.labels.componentId")
            checkDuplicates(guide.pages.map { it.child }, "skillguides.${guide.skillId}.pages.child")
            guide.pages.forEach { page ->
                check(page.names.isNotEmpty()) { "skill guide ${guide.skillId} page ${page.child} must define names" }
                if (page.levels.isNotEmpty()) {
                    check(page.levels.size <= page.names.size) {
                        "skill guide ${guide.skillId} page ${page.child} levels size must be <= names size"
                    }
                }
                if (page.items.isNotEmpty()) {
                    check(page.items.size <= page.names.size) {
                        "skill guide ${guide.skillId} page ${page.child} items size must be <= names size"
                    }
                }
                if (page.amounts.isNotEmpty()) {
                    check(page.amounts.size <= page.names.size) {
                        "skill guide ${guide.skillId} page ${page.child} amounts size must be <= names size"
                    }
                }
                validateItemIds(
                    page.items.filter { it > 0 },
                    "content/skills/guides/$guideKey.toml:page.${page.child}",
                    ValidationSeverity.WARN,
                    warnings,
                )
            }
            guide.specialAfterFramesPage?.let { page ->
                check(page.names.isNotEmpty()) { "skill guide ${guide.skillId} specialAfterFramesPage must define names" }
                validateItemIds(
                    page.items.filter { it > 0 },
                    "content/skills/guides/$guideKey.toml:specialAfterFramesPage",
                    ValidationSeverity.WARN,
                    warnings,
                )
            }
        }

        reportOverlaps(
            listOf(
                "objects.travel.passage" to travel.passageObjects.toList(),
                "objects.travel.teleport" to travel.teleportObjects.toList(),
                "objects.travel.webObstacle" to travel.webObstacleObjects.toList(),
                "objects.skill.crafting.resourceFill" to craftingResourceFillObjects,
                "objects.skill.crafting.spinningWheel" to craftingSpinningWheelObjects,
                "objects.skill.cooking.range" to cookingRangeObjects,
                "objects.skill.smithing.anvil" to smithingAnvilObjects,
                "objects.skill.smithing.furnace" to smithingFurnaceObjects,
                "objects.skill.smithing.interfaceFurnace" to smithingSmeltingInterfaceFurnaces,
                "objects.skill.farming.patchGuide" to farmingPatchGuideObjects,
                "objects.skill.runecrafting.altar" to runecraftingAltarObjects,
                "objects.banking.booth" to bankingObjects.boothObjects.toList(),
                "objects.banking.chest" to bankingObjects.chestObjects.toList(),
                "objects.partyroom.balloon" to partyroomObjects.balloonObjects.toList(),
            ),
            warnings,
        )

        reportOverlaps(
            listOf(
                "interfaces.ui" to (
                    ui.runOffButtons.toList() +
                        ui.runOnButtons.toList() +
                        ui.runToggleButtons.toList() +
                        ui.tabInterfaceDefaultButtons.toList() +
                        ui.tabInterfaceEquipmentButtons.toList() +
                        ui.sidebarHomeButtons.toList() +
                        ui.closeInterfaceButtons.toList() +
                        ui.questTabToggleButtons.toList() +
                        ui.logoutButtons.toList() +
                        ui.morphButtons.toList()
                    ),
                "interfaces.dialogue" to (
                    dialogue.optionOne.toList() +
                        dialogue.optionTwo.toList() +
                        dialogue.optionThree.toList() +
                        dialogue.optionFour.toList() +
                        dialogue.optionFive.toList() +
                        dialogue.toggleSpecialsButtons.toList() +
                        dialogue.toggleBossYellButtons.toList()
                    ),
                "interfaces.bank" to (
                    bank.depositInventoryButtons.toList() +
                        bank.depositWornItemsButtons.toList() +
                        bank.withdrawAsNoteButtons.toList() +
                        bank.withdrawAsItemButtons.toList() +
                        bank.searchButtons.toList() +
                        bank.tabButtons.toList()
                    ),
                "interfaces.settings" to (
                    settings.openMoreSettingsButtons.toList() +
                        settings.closeMoreSettingsButtons.toList() +
                        settings.pinHelpButtons.toList() +
                        settings.bossYellEnableButtons.toList() +
                        settings.bossYellDisableButtons.toList()
                    ),
                "interfaces.emotes" to (
                    emotes.goblinBowButtons.toList() +
                        emotes.goblinSaluteButtons.toList() +
                        emotes.glassBoxButtons.toList() +
                        emotes.climbRopeButtons.toList() +
                        emotes.leanButtons.toList() +
                        emotes.glassWallButtons.toList() +
                        emotes.ideaButtons.toList() +
                        emotes.stompButtons.toList() +
                        emotes.skillcapeButtons.toList()
                    ),
                "interfaces.skillguide" to (
                    skillGuide.skillButtons.flatMap { it.rawButtonIds.toList() } +
                        skillGuide.subTabs.flatMap { it.rawButtonIds.toList() }
                    ),
            ),
            warnings,
        )

        emitSummary(domainCounts, warnings)
    }

    private fun checkDuplicates(values: List<Any>, label: String) {
        val duplicates = values.groupBy { it }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw IllegalStateException("Duplicate values in $label: ${duplicates.joinToString(",")}")
        }
    }

    private fun validateItemIds(
        itemIds: List<Int>,
        label: String,
        severity: ValidationSeverity = ValidationSeverity.ERROR,
        warnings: MutableList<String> = mutableListOf(),
    ) {
        val itemManager = Server.itemManager ?: return
        val missing = itemIds.filter { !itemManager.items.containsKey(it) }.distinct().sorted()
        if (missing.isNotEmpty()) {
            val message = "Unknown item ids in $label: ${missing.joinToString(",")}"
            if (severity == ValidationSeverity.ERROR) {
                throw IllegalStateException(message)
            }
            warnings += message
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

    private fun reportOverlaps(
        groups: List<Pair<String, List<Int>>>,
        warnings: MutableList<String>,
    ) {
        val usage = linkedMapOf<Int, MutableSet<String>>()
        groups.forEach { (source, ids) ->
            ids.distinct().forEach { id ->
                usage.computeIfAbsent(id) { linkedSetOf() }.add(source)
            }
        }
        usage
            .filterValues { it.size > 1 }
            .toSortedMap()
            .forEach { (id, sources) ->
                warnings += "Overlapping mapping id=$id across ${sources.joinToString(",")}"
            }
    }

    private fun emitSummary(
        domainCounts: Map<String, Int>,
        warnings: List<String>,
    ) {
        val summary =
            domainCounts
                .toSortedMap()
                .entries
                .joinToString(", ") { "${it.key}=${it.value}" }
        logger.info("Content validation report: {}", summary)
        if (warnings.isEmpty()) {
            logger.info("Content validation warnings: none")
            return
        }
        logger.warn("Content validation warnings count={}", warnings.size)
        warnings.sorted().forEach { logger.warn("Content validation warning: {}", it) }
    }
}
