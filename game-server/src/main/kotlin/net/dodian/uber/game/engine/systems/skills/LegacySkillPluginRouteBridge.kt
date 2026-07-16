package net.dodian.uber.game.engine.systems.skills

import net.dodian.uber.game.item.ItemContent
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.api.interaction.ObjectInteractionContext
import net.dodian.uber.game.api.interaction.InteractionOption
import net.dodian.uber.game.api.interaction.ItemPayload
import net.dodian.uber.game.api.interaction.SpellPayload
import net.dodian.uber.game.api.plugin.skills.ItemClickOption
import net.dodian.uber.game.api.plugin.skills.SkillPluginBuilder

fun SkillPluginBuilder.bindObjectContentClick(
    preset: PolicyPreset,
    option: Int,
    content: ObjectContent,
) {
    objectClick(preset = preset, option = option, *content.objectIds) { interaction ->
        val opt = when (option) {
            1 -> InteractionOption.FIRST
            2 -> InteractionOption.SECOND
            3 -> InteractionOption.THIRD
            4 -> InteractionOption.FOURTH
            5 -> InteractionOption.FIFTH
            else -> InteractionOption.FIRST
        }
        val context = ObjectInteractionContext(
            player = interaction.player.protocolClient(),
            option = opt,
            objectId = interaction.objectId,
            position = interaction.position,
            definition = interaction.definition
        )
        when (option) {
            1 -> content.onFirstClick(context)
            2 -> content.onSecondClick(context)
            3 -> content.onThirdClick(context)
            4 -> content.onFourthClick(context)
            5 -> content.onFifthClick(context)
            else -> false
        }
    }
}

fun SkillPluginBuilder.bindObjectContentUseItem(
    preset: PolicyPreset,
    content: ObjectContent,
    itemIds: IntArray = intArrayOf(-1),
) {
    itemOnObject(preset = preset, *content.objectIds, itemIds = itemIds) { interaction ->
        val context = ObjectInteractionContext(
            player = interaction.player.protocolClient(),
            option = InteractionOption.USE_ITEM,
            objectId = interaction.objectId,
            position = interaction.position,
            definition = interaction.definition,
            itemPayload = ItemPayload(interaction.itemId, interaction.itemSlot, interaction.interfaceId)
        )
        content.onUseItem(context)
    }
}

fun SkillPluginBuilder.bindObjectContentMagic(
    preset: PolicyPreset,
    content: ObjectContent,
    spellIds: IntArray = intArrayOf(-1),
) {
    magicOnObject(preset, *content.objectIds, spellIds = spellIds) { interaction ->
        val context = ObjectInteractionContext(
            player = interaction.player.protocolClient(),
            option = InteractionOption.MAGIC,
            objectId = interaction.objectId,
            position = interaction.position,
            definition = interaction.definition,
            spellPayload = SpellPayload(interaction.spellId)
        )
        content.onMagic(context)
    }
}

fun SkillPluginBuilder.bindItemContentClick(
    preset: PolicyPreset,
    option: Int,
    content: ItemContent,
) {
    itemClick(preset = preset, option = option, *content.itemIds) { interaction ->
        when (option) {
            1 -> content.onFirstClick(interaction.player.protocolClient(), interaction.itemId, interaction.itemSlot, interaction.interfaceId)
            2 -> content.onSecondClick(interaction.player.protocolClient(), interaction.itemId, interaction.itemSlot, interaction.interfaceId)
            3 -> content.onThirdClick(interaction.player.protocolClient(), interaction.itemId, interaction.itemSlot, interaction.interfaceId)
            else -> false
        }
    }
}

fun SkillPluginBuilder.bindItemContentClicks(
    preset: PolicyPreset,
    content: ItemContent,
    vararg options: ItemClickOption,
) {
    require(options.isNotEmpty()) { "Item content binding requires at least one option." }
    options.forEach { option ->
        bindItemContentClick(
            preset = preset,
            option = option.id,
            content = content,
        )
    }
}
