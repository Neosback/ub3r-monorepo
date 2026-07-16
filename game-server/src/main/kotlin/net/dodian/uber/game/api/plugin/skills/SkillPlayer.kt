package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.api.content.ContentActionControl
import net.dodian.uber.game.api.content.ContentEconomy
import net.dodian.uber.game.api.content.ContentEquipment
import net.dodian.uber.game.api.content.ContentFeatures
import net.dodian.uber.game.api.content.ContentInventory
import net.dodian.uber.game.api.content.ContentPlayer
import net.dodian.uber.game.api.content.ContentSocial
import net.dodian.uber.game.api.content.ContentUi
import net.dodian.uber.game.api.content.ContentWorld

/**
 * The player surface available to skill content.
 *
 * Network/protocol details deliberately stay behind this boundary.  The
 * concrete adapter is created by the skill dispatcher for one interaction.
 */
interface SkillPlayer : ContentPlayer {
    val skills: SkillLevels
    override val inventory: SkillInventory
    override val actions: SkillActions
    override val ui: SkillUi
    override val world: SkillWorld
}

interface SkillLevels {
    fun current(skill: Skill): Int
    fun base(skill: Skill): Int
    fun experience(skill: Skill): Int
    fun gainXp(amount: Int, skill: Skill): Boolean
}

interface SkillInventory : ContentInventory {
    fun transaction(block: SkillInventoryTransaction.() -> Unit): Boolean
    override fun refresh()
}

interface SkillInventoryTransaction {
    fun require(itemId: Int, amount: Int = 1): Boolean
    fun remove(itemId: Int, amount: Int = 1): Boolean
    fun removeAt(slot: Int, itemId: Int, amount: Int = 1): Boolean
    fun add(itemId: Int, amount: Int = 1): Boolean
}

interface SkillActions : ContentActionControl

interface SkillUi : ContentUi

interface SkillWorld : ContentWorld
