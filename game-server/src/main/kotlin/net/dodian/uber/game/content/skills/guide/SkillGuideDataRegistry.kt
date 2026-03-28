package net.dodian.uber.game.content.skills.guide

import net.dodian.uber.game.content.platform.ContentDataLoader
import net.dodian.uber.game.model.player.skills.Skill

data class SkillGuideTomlLabel(
    val componentId: Int,
    val text: String,
)

data class SkillGuideTomlPage(
    val child: Int,
    val names: List<String> = emptyList(),
    val levels: List<String> = emptyList(),
    val items: List<Int> = emptyList(),
    val amounts: List<Int> = emptyList(),
)

data class SkillGuideTomlFile(
    val skillId: Int,
    val labels: List<SkillGuideTomlLabel> = emptyList(),
    val showComponents: List<Int> = emptyList(),
    val hideComponents: List<Int> = emptyList(),
    val pages: List<SkillGuideTomlPage> = emptyList(),
    val specialAfterFramesPage: SkillGuideTomlPage? = null,
)

object SkillGuideDataRegistry {
    private val keyBySkillId: Map<Int, String> =
        mapOf(
            Skill.ATTACK.id to "attack",
            Skill.DEFENCE.id to "defence",
            Skill.STRENGTH.id to "strength",
            Skill.HITPOINTS.id to "hitpoints",
            Skill.RANGED.id to "ranged",
            Skill.PRAYER.id to "prayer",
            Skill.MAGIC.id to "magic",
            Skill.THIEVING.id to "thieving",
            Skill.RUNECRAFTING.id to "runecrafting",
            Skill.FISHING.id to "fishing",
            Skill.COOKING.id to "cooking",
            Skill.CRAFTING.id to "crafting",
            Skill.SMITHING.id to "smithing",
            Skill.AGILITY.id to "agility",
            Skill.WOODCUTTING.id to "woodcutting",
            Skill.MINING.id to "mining",
            Skill.SLAYER.id to "slayer",
            Skill.FIREMAKING.id to "firemaking",
            Skill.HERBLORE.id to "herblore",
            Skill.FLETCHING.id to "fletching",
            Skill.FARMING.id to "farming",
        )

    @Volatile
    private var cache: Map<Int, SkillGuideTomlFile>? = null

    @JvmStatic
    fun all(): Map<Int, SkillGuideTomlFile> {
        val loaded = cache
        if (loaded != null) return loaded
        val built = keyBySkillId.mapValues { (skillId, key) ->
            ContentDataLoader.loadRequired<SkillGuideTomlFile>("content/skills/guides/$key.toml").also {
                require(it.skillId == skillId) {
                    "guide file content/skills/guides/$key.toml has skillId=${it.skillId}, expected=$skillId"
                }
            }
        }
        cache = built
        return built
    }

    @JvmStatic
    fun keyForSkillId(skillId: Int): String? = keyBySkillId[skillId]
}
