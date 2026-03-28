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
    fun magicData(): MagicDataFile {
        val loaded = magicData ?: ContentDataLoader.loadRequired<MagicDataFile>("content/interfaces/magic.toml").also {
            magicData = it
        }
        return loaded
    }

    @JvmStatic
    fun skillGuideData(): SkillGuideDataFile {
        val loaded = skillGuideData ?: ContentDataLoader.loadRequired<SkillGuideDataFile>("content/interfaces/skillguide.toml").also {
            skillGuideData = it
        }
        return loaded
    }

    @JvmStatic
    fun travelData(): TravelObjectsDataFile {
        val loaded = travelData ?: ContentDataLoader.loadRequired<TravelObjectsDataFile>("content/objects/travel.toml").also {
            travelData = it
        }
        return loaded
    }
}
