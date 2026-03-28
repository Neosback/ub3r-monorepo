package net.dodian.uber.game.content.skills.smithing.objects

import net.dodian.uber.game.content.platform.SkillDataRegistry

object SmithingObjectComponents {
    val anvilObjects: IntArray
        get() = SkillDataRegistry.smithingAnvilObjects()
    val furnaceObjects: IntArray
        get() = SkillDataRegistry.smithingFurnaceObjects()
    val smeltingInterfaceFurnaces: IntArray
        get() = SkillDataRegistry.smithingSmeltingInterfaceFurnaces()
}
