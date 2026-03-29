package net.dodian.game.systems.interaction.scheduler

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.systems.interaction.InteractionIntent

class NpcInteractionTask(
    player: Client,
    intent: InteractionIntent,
) : InteractionQueueTask(player, intent, InteractionRoutePolicy.NPC)
