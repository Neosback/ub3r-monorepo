package net.dodian.uber.game.content.objects.travel

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry
import net.dodian.uber.game.content.platform.TravelObjectsDataFile

object TravelObjectComponents {
    private val fallbackPassageObjects = intArrayOf(
        882,
        1521, 1524,
        2309, 23140, 23271, 23564,
        2391, 2392,
        2623, 2624, 2625, 2634,
        15656, 16466, 16509, 16510,
    )

    private val fallbackTeleportObjects = intArrayOf(
        823,
        133,
        410,
        1294,
        1591,
        2352,
        2492,
        5960,
        9368, 9369,
        11833, 11834,
        11635,
        12260,
        14914,
        14847,
        16519, 16520,
        16680,
        17384, 17385, 17387,
        2156, 2158,
        20877,
        5553, 6702,
        6451, 6452,
    )

    private val fallbackWebObstacleObjects = intArrayOf(733)

    private val loadedData: TravelObjectsDataFile by lazy {
        InterfaceMappingRegistry.travelData(
            TravelObjectsDataFile(
                passageObjects = fallbackPassageObjects,
                teleportObjects = fallbackTeleportObjects,
                webObstacleObjects = fallbackWebObstacleObjects,
            ),
        )
    }

    val passageObjects: IntArray
        get() = loadedData.passageObjects

    val teleportObjects: IntArray
        get() = loadedData.teleportObjects

    val webObstacleObjects: IntArray
        get() = loadedData.webObstacleObjects
}
