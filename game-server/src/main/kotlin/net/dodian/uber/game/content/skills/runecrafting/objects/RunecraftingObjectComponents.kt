package net.dodian.uber.game.content.skills.runecrafting.objects

import net.dodian.uber.game.content.platform.SkillDataRegistry

object RunecraftingObjectComponents {
    const val NATURE_ALTAR = 14905
    const val BLOOD_ALTAR = 27978
    const val FIRE_ALTAR = 14903
    val altarObjects: IntArray
        get() = SkillDataRegistry.runecraftingAltarObjects()
}
