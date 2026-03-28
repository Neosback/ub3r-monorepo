package net.dodian.uber.game.content.skills.slayer

import net.dodian.uber.game.content.platform.SkillDataRegistry

object SlayerDefinitions {
    val mazchna: Array<SlayerTaskDefinition>
        get() = SkillDataRegistry.slayerMazchnaTasks()

    val vannaka: Array<SlayerTaskDefinition>
        get() = SkillDataRegistry.slayerVannakaTasks()

    val duradel: Array<SlayerTaskDefinition>
        get() = SkillDataRegistry.slayerDuradelTasks()
}
