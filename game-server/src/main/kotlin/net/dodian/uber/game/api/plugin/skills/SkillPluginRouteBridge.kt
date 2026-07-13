package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.item.ItemContent
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.api.interaction.ObjectInteractionContext
import net.dodian.uber.game.api.interaction.InteractionOption
import net.dodian.uber.game.api.interaction.ItemPayload
import net.dodian.uber.game.api.interaction.SpellPayload

fun SkillPluginBuilder.bindObjectContentClick(
    preset: PolicyPreset,
    option: Int,
    content: ObjectContent,
) {
    objectClick(preset = preset, option = option, *content.objectIds) { client, objectId, position, obj ->
        val opt = when (option) {
            1 -> InteractionOption.FIRST
            2 -> InteractionOption.SECOND
            3 -> InteractionOption.THIRD
            4 -> InteractionOption.FOURTH
            5 -> InteractionOption.FIFTH
            else -> InteractionOption.FIRST
        }
        val context = ObjectInteractionContext(
            player = client,
            option = opt,
            objectId = objectId,
            position = position,
            definition = obj
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
    itemOnObject(preset = preset, *content.objectIds, itemIds = itemIds) { client, objectId, position, obj, itemId, itemSlot, interfaceId ->
        val context = ObjectInteractionContext(
            player = client,
            option = InteractionOption.USE_ITEM,
            objectId = objectId,
            position = position,
            definition = obj,
            itemPayload = ItemPayload(itemId, itemSlot, interfaceId)
        )
        content.onUseItem(context)
    }
}

fun SkillPluginBuilder.bindObjectContentMagic(
    preset: PolicyPreset,
    content: ObjectContent,
    spellIds: IntArray = intArrayOf(-1),
) {
    magicOnObject(preset, *content.objectIds, spellIds = spellIds) { client, objectId, position, obj, spellId ->
        val context = ObjectInteractionContext(
            player = client,
            option = InteractionOption.MAGIC,
            objectId = objectId,
            position = position,
            definition = obj,
            spellPayload = SpellPayload(spellId)
        )
        content.onMagic(context)
    }
}

fun SkillPluginBuilder.bindItemContentClick(
    preset: PolicyPreset,
    option: Int,
    content: ItemContent,
) {
    itemClick(preset = preset, option = option, *content.itemIds) { client, itemId, itemSlot, interfaceId ->
        when (option) {
            1 -> content.onFirstClick(client, itemId, itemSlot, interfaceId)
            2 -> content.onSecondClick(client, itemId, itemSlot, interfaceId)
            3 -> content.onThirdClick(client, itemId, itemSlot, interfaceId)
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