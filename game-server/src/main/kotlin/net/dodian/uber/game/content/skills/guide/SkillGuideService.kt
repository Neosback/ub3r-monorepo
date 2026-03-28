package net.dodian.uber.game.content.skills.guide

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces
import net.dodian.uber.game.netty.listener.out.SendMessage
import net.dodian.uber.game.netty.listener.out.ShowInterface

object SkillGuideService {
    @JvmStatic
    fun open(client: Client, skillId: Int, child: Int) {
        val switchingSkill = client.currentSkill != skillId
        if (switchingSkill) {
            client.send(RemoveInterfaces())
        }
        if (client.isBusy) {
            client.sendMessage("You are currently too busy to open the skill menu!")
            return
        }

        val definition = SkillGuideDefinitions.find(skillId) ?: return
        val skill = Skill.getSkill(skillId) ?: return

        clearInterface(client)
        resetBaselineVisibility(client, skillId)

        val skillName = skill.name.lowercase().replaceFirstChar { it.uppercase() }
        client.sendString(skillName, 8716)

        definition.layout.hideComponents.forEach { client.changeInterfaceStatus(it, false) }
        definition.layout.showComponents.forEach { client.changeInterfaceStatus(it, true) }
        definition.layout.extraStrings.forEach { (componentId, text) -> client.sendString(text, componentId) }
        definition.tabLabels.forEach { label -> client.sendString(label.text, label.componentId) }

        val page = definition.pageProvider(client, child) ?: SkillGuidePage()
        page.entries.forEachIndexed { index, entry ->
            client.sendString(entry.text, 8760 + index)
            entry.levelText?.let { client.sendString(it, 8720 + index) }
        }

        val itemIds = page.entries.map { it.itemId }.toIntArray()
        val hasAmounts = page.entries.any { it.itemAmount != null }
        if (hasAmounts) {
            val amounts = page.entries.map { it.itemAmount ?: 0 }.toIntArray()
            client.setMenuItems(itemIds, amounts)
        } else {
            client.setMenuItems(itemIds)
        }

        client.sendQuestSomething(8717)
        if (switchingSkill) {
            client.send(ShowInterface(8714))
        }
        client.currentSkill = skillId
    }

    private fun clearInterface(client: Client) {
        InterfaceMappingRegistry.skillGuideData().titleComponentIds.forEach { componentId -> client.sendString("", componentId) }
        for (componentId in 8720 until 8800) {
            client.sendString("", componentId)
        }
    }

    private fun resetBaselineVisibility(client: Client, skillId: Int) {
        if (skillId >= 23) {
            return
        }
        InterfaceMappingRegistry.skillGuideData().baselineHidden.forEach { componentId -> client.changeInterfaceStatus(componentId, false) }
        InterfaceMappingRegistry.skillGuideData().baselineShown.forEach { componentId -> client.changeInterfaceStatus(componentId, true) }
        client.sendString("", 8849)
    }
}
