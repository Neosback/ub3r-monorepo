package net.dodian.uber.game.content.skills.thieving

import net.dodian.uber.game.content.platform.SkillDataRegistry

object ThievingDefinitions {
    val all: Array<ThievingDefinition>
        get() = SkillDataRegistry.thievingDefinitions().toTypedArray()

    val stallObjects: IntArray
        get() = SkillDataRegistry.thievingStallObjects()

    val chestObjects: IntArray
        get() = SkillDataRegistry.thievingChestObjects()

    val plunderObjects: IntArray
        get() = SkillDataRegistry.thievingPlunderObjects()

    @JvmStatic
    fun forId(entityId: Int): ThievingDefinition? = SkillDataRegistry.thievingDefinitions().firstOrNull { it.entityId == entityId }
}
