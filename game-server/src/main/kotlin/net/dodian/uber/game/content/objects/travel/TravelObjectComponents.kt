package net.dodian.uber.game.content.objects.travel

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object TravelObjectComponents {
    private val loadedData by lazy { InterfaceMappingRegistry.travelData() }

    val passageObjects: IntArray
        get() = loadedData.passageObjects

    val teleportObjects: IntArray
        get() = loadedData.teleportObjects

    val webObstacleObjects: IntArray
        get() = loadedData.webObstacleObjects
}
