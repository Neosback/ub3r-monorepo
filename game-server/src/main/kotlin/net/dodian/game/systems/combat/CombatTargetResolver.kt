package net.dodian.game.systems.combat

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.systems.world.player.PlayerRegistry

internal fun resolveCombatTargetPlayer(slot: Int): Client? = PlayerRegistry.getClient(slot)
