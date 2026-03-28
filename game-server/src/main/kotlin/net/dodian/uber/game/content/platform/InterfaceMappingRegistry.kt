package net.dodian.uber.game.content.platform

import net.dodian.uber.game.content.interfaces.magic.MagicComponents
import net.dodian.uber.game.content.interfaces.skillguide.SkillGuideComponents

data class MagicDataFile(
    val spellbookToggleButtons: IntArray = intArrayOf(),
    val autocastClearButtons: IntArray = intArrayOf(),
    val autocastSelectButtons: IntArray = intArrayOf(),
    val autocastRefreshButtons: IntArray = intArrayOf(),
    val teleports: List<MagicComponents.TeleportBinding> = emptyList(),
)

data class SkillGuideDataFile(
    val skillButtons: List<SkillGuideComponents.SkillGuideButtonGroup> = emptyList(),
    val subTabs: List<SkillGuideComponents.SubTabDefinition> = emptyList(),
)

data class TravelObjectsDataFile(
    val passageObjects: IntArray = intArrayOf(),
    val teleportObjects: IntArray = intArrayOf(),
    val webObstacleObjects: IntArray = intArrayOf(),
)

object InterfaceMappingRegistry {
    @Volatile
    private var magicData: MagicDataFile? = null

    @Volatile
    private var skillGuideData: SkillGuideDataFile? = null

    @Volatile
    private var travelData: TravelObjectsDataFile? = null

    @JvmStatic
    fun magicData(defaults: MagicDataFile): MagicDataFile {
        val loaded = magicData ?: ContentDataLoader.loadOptional<MagicDataFile>("content/interfaces/magic.toml").also {
            magicData = it
        }
        return loaded ?: defaults
    }

    @JvmStatic
    fun skillGuideData(defaults: SkillGuideDataFile): SkillGuideDataFile {
        val loaded = skillGuideData ?: ContentDataLoader.loadOptional<SkillGuideDataFile>("content/interfaces/skillguide.toml").also {
            skillGuideData = it
        }
        return loaded ?: defaults
    }

    @JvmStatic
    fun travelData(defaults: TravelObjectsDataFile): TravelObjectsDataFile {
        val loaded = travelData ?: ContentDataLoader.loadOptional<TravelObjectsDataFile>("content/objects/travel.toml").also {
            travelData = it
        }
        return loaded ?: defaults
    }
}
