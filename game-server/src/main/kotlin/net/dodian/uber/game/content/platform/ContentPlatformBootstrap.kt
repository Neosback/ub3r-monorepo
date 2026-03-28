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
        val cooking = SkillDataRegistry.cookingRecipes(emptyList())
        checkDuplicates(cooking.map { it.rawItemId }, "cooking.rawItemId")
        validateItemIds(cooking.flatMap { listOf(it.rawItemId, it.cookedItemId, it.burntItemId) }, "cooking")

        val fishing = SkillDataRegistry.fishingSpots(emptyList())
        checkDuplicates(fishing.map { "${it.objectId}:${it.clickType}" }, "fishing.objectId+clickType")
        checkDuplicates(fishing.map { it.index }, "fishing.index")
        validateItemIds(fishing.map { it.fishItemId } + fishing.map { it.toolItemId }, "fishing")

        val fletchBow = SkillDataRegistry.fletchingBowLogs(emptyList())
        val fletchArrow = SkillDataRegistry.fletchingArrowRecipes(emptyList())
        val fletchDart = SkillDataRegistry.fletchingDartRecipes(emptyList())
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

        val magic =
            InterfaceMappingRegistry.magicData(
                MagicDataFile(
                    spellbookToggleButtons = intArrayOf(),
                    autocastClearButtons = intArrayOf(),
                    autocastSelectButtons = intArrayOf(),
                    autocastRefreshButtons = intArrayOf(),
                    teleports = emptyList(),
                ),
            )
        checkDuplicates(
            magic.teleports.flatMap { it.rawButtonIds.toList() },
            "magic.teleport.rawButtonIds",
        )
        checkDuplicates(magic.teleports.map { it.componentId }, "magic.teleport.componentId")

        val skillGuide =
            InterfaceMappingRegistry.skillGuideData(
                SkillGuideDataFile(
                    skillButtons = emptyList(),
                    subTabs = emptyList(),
                ),
            )
        checkDuplicates(skillGuide.skillButtons.map { it.skillId }, "skillguide.skillId")
        checkDuplicates(
            skillGuide.skillButtons.flatMap { it.rawButtonIds.toList() } + skillGuide.subTabs.flatMap { it.rawButtonIds.toList() },
            "skillguide.rawButtonIds",
        )

        val travel =
            InterfaceMappingRegistry.travelData(
                TravelObjectsDataFile(
                    passageObjects = intArrayOf(),
                    teleportObjects = intArrayOf(),
                    webObstacleObjects = intArrayOf(),
                ),
            )
        checkDuplicates(travel.passageObjects.toList(), "travel.passageObjects")
        checkDuplicates(travel.teleportObjects.toList(), "travel.teleportObjects")
        checkDuplicates(travel.webObstacleObjects.toList(), "travel.webObstacleObjects")
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
