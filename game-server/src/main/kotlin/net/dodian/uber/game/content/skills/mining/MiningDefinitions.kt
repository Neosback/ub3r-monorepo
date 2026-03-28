package net.dodian.uber.game.content.skills.mining

import net.dodian.uber.game.content.platform.SkillDataRegistry

enum class RockCategory {
    STANDARD,
    GEM,
    SPECIAL,
}

data class MiningRockDef(
    val name: String,
    val objectIds: IntArray,
    val requiredLevel: Int,
    val baseDelayMs: Long,
    val oreItemId: Int,
    val experience: Int,
    val randomGemEligible: Boolean = true,
    val restThreshold: Int,
    val category: RockCategory = RockCategory.STANDARD,
)

data class PickaxeDef(
    val name: String,
    val itemId: Int,
    val requiredLevel: Int,
    val speedBonus: Double,
    val animationId: Int,
    val dragonTierBoostEligible: Boolean,
)

object MiningDefinitions {
    val rocks: List<MiningRockDef>
        get() = SkillDataRegistry.miningRocks()

    val rockByObjectId: Map<Int, MiningRockDef> =
        buildMap {
            rocks.forEach { rock ->
                rock.objectIds.forEach { objectId ->
                    put(objectId, rock)
                }
            }
        }

    val allRockObjectIds: IntArray = rockByObjectId.keys.sorted().toIntArray()

    val pickaxesDescending: List<PickaxeDef>
        get() = SkillDataRegistry.miningPickaxes()

    val pickaxeByItemId: Map<Int, PickaxeDef> = pickaxesDescending.associateBy { it.itemId }

    val randomGemDropTable: IntArray
        get() = SkillDataRegistry.miningRandomGemDropTable()
}
