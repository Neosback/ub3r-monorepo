package net.dodian.uber.game.content.skills.woodcutting

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class TreeDef(
    val objectIds: IntArray,
    val logItemId: Int,
    val requiredLevel: Int,
    val experience: Int,
    val baseDelayMs: Long,
) 

data class AxeDef(
    val itemId: Int,
    val requiredLevel: Int,
    val speedBonus: Double,
    val animationId: Int,
    val dragonTierBoostEligible: Boolean = false,
) 

object WoodcuttingDefinitions {
    val trees: List<TreeDef>
        get() = SkillDataRegistry.woodcuttingTrees()

    val axesDescending: List<AxeDef>
        get() = SkillDataRegistry.woodcuttingAxes()

    val treeByObjectId: Map<Int, TreeDef> =
        buildMap {
            trees.forEach { tree ->
                tree.objectIds.forEach { objectId ->
                    put(objectId, tree)
                }
            }
        }

    val axeByItemId: Map<Int, AxeDef> = axesDescending.associateBy { it.itemId }

    val allTreeObjectIds: IntArray = treeByObjectId.keys.sorted().toIntArray()
}
