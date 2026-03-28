package net.dodian.uber.game.content.skills.crafting.objects

import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.model.entity.player.Client

object ResourceFillingService {
    private val sourceByObjectId: Map<Int, net.dodian.uber.game.content.platform.ResourceFillSourceDefinition>
        get() =
            SkillDataRegistry
                .craftingResourceFillSources()
                .flatMap { source -> source.objectIds.map { objectId -> objectId to source } }
                .toMap()

    @JvmStatic
    fun handleObjectUse(client: Client, objectId: Int): Boolean {
        val source = sourceByObjectId[objectId] ?: return false

        for (entry in source.entries) {
            if (!client.playerHasItem(entry.emptyItemId)) {
                continue
            }
            client.deleteItem(entry.emptyItemId, 1)
            client.addItem(entry.filledItemId, 1)
            client.checkItemUpdate()
            client.performAnimation(entry.emote, 0)
            return true
        }
        return true
    }
}
