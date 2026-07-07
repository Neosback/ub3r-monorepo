package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object Horvik : NpcFamily by npcFamily("Horvik", 2882, block = {
    definition {
        examine = "Yar! Shiver me timbers! (Pirate's cove)  Avast ye scurvy land lubbers! (Brimhaven)  This one's had too much to drink! (Asgarnian Ice Dungeon)"
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleHorvikTalkTo)
        third("trade", ::handleHorvikTrade)
    }

    spawns {
        spawn(2615, 3083)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleHorvikTalkTo(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.WEAPON_AND_ARMOR.id)
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleHorvikTrade(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.WEAPON_AND_ARMOR.id)
    return true
}
