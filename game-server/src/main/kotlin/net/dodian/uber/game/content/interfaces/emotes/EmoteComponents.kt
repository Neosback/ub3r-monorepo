package net.dodian.uber.game.content.interfaces.emotes

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry
import net.dodian.uber.game.model.entity.player.Emotes

object EmoteComponents {
    val standardEmoteButtons: IntArray = Emotes.values().map { it.buttonId }.toIntArray()
    val goblinBowButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().goblinBowButtons
    val goblinSaluteButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().goblinSaluteButtons
    val glassBoxButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().glassBoxButtons
    val climbRopeButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().climbRopeButtons
    val leanButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().leanButtons
    val glassWallButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().glassWallButtons
    val ideaButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().ideaButtons
    val stompButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().stompButtons
    val skillcapeButtons: IntArray
        get() = InterfaceMappingRegistry.emoteData().skillcapeButtons
}
