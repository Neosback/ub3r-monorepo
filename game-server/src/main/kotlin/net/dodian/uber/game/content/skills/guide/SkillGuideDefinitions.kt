package net.dodian.uber.game.content.skills.guide

import net.dodian.uber.game.content.skills.fletching.FletchingDefinitions
import net.dodian.uber.game.content.skills.herblore.HerbloreDefinitions
import net.dodian.uber.game.content.skills.smithing.SmithingFrameDefinitions
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.player.skills.Skill

object SkillGuideDefinitions {
    @JvmStatic
    fun find(skillId: Int): SkillGuideDefinition? {
        val data = SkillGuideDataRegistry.all()[skillId] ?: return null
        val staticPages = data.pages.associateBy { it.child }
        return SkillGuideDefinition(
            skillId = data.skillId,
            tabLabels = data.labels.map { SkillGuideTabLabel(it.componentId, it.text) },
            layout = SkillGuideLayout(
                showComponents = data.showComponents.toIntArray(),
                hideComponents = data.hideComponents.toIntArray(),
            ),
            pageProvider = { client, child ->
                dynamicPage(skillId, client, child, data) ?: staticPages[child]?.toPage(client, skillId) ?: SkillGuidePage()
            },
        )
    }

    private fun dynamicPage(skillId: Int, client: Client, child: Int, data: SkillGuideTomlFile): SkillGuidePage? {
        if (skillId == Skill.SMITHING.id) {
            if (child > 0 && child <= SmithingFrameDefinitions.smithingFrame.size) {
                val frame = SmithingFrameDefinitions.smithingFrame[child - 1]
                return SkillGuidePage(
                    frame.map { definition ->
                        SkillGuideEntry(
                            text = client.getItemName(definition.itemId),
                            levelText = definition.levelRequired.toString(),
                            itemId = definition.itemId,
                            itemAmount = definition.outputAmount,
                        )
                    },
                )
            }
            if (child == SmithingFrameDefinitions.smithingFrame.size + 1) {
                return data.specialAfterFramesPage?.toPage(client, skillId)
            }
        }

        if (skillId == Skill.HERBLORE.id && child == 1) {
            return SkillGuidePage(
                HerbloreDefinitions.herbDefinitions.map { definition ->
                    SkillGuideEntry(
                        text = client.getItemName(definition.cleanId) + if (definition.premiumOnly) " @red@(Premium only)" else "",
                        levelText = definition.requiredLevel.toString(),
                        itemId = definition.grimyId,
                    )
                },
            )
        }

        if (skillId == Skill.FLETCHING.id && child == 0) {
            val page = data.pages.firstOrNull { it.child == child } ?: return null
            val dynamicItems = intArrayOf(
                52,
                FletchingDefinitions.bowLogs[0].unstrungShortbowId,
                FletchingDefinitions.bowLogs[0].unstrungLongbowId,
                FletchingDefinitions.bowLogs[1].unstrungShortbowId,
                FletchingDefinitions.bowLogs[1].unstrungLongbowId,
                FletchingDefinitions.bowLogs[2].unstrungShortbowId,
                FletchingDefinitions.bowLogs[2].unstrungLongbowId,
                FletchingDefinitions.bowLogs[3].unstrungShortbowId,
                FletchingDefinitions.bowLogs[3].unstrungLongbowId,
                FletchingDefinitions.bowLogs[4].unstrungShortbowId,
                FletchingDefinitions.bowLogs[4].unstrungLongbowId,
            )
            return buildPage(page.names, page.levels, dynamicItems.toList(), page.amounts)
        }

        return null
    }

    private fun SkillGuideTomlPage.toPage(client: Client, skillId: Int): SkillGuidePage {
        val names = if (skillId == Skill.SMITHING.id && child == 0) {
            this.names.map {
                it.replace("{iron_success}", (50 + ((client.getLevel(Skill.SMITHING) + 1) / 4)).toString())
            }
        } else {
            this.names
        }
        return buildPage(names, levels, items, amounts)
    }

    private fun buildPage(
        names: List<String>,
        levels: List<String>,
        items: List<Int>,
        amounts: List<Int>,
    ): SkillGuidePage =
        SkillGuidePage(
            names.indices.map { index ->
                SkillGuideEntry(
                    text = names[index],
                    levelText = levels.getOrNull(index),
                    itemId = items.getOrElse(index) { -1 },
                    itemAmount = amounts.getOrNull(index),
                )
            },
        )
}

