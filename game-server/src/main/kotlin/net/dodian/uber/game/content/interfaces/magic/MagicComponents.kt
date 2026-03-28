package net.dodian.uber.game.content.interfaces.magic

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object MagicComponents {
    const val NORMAL_INTERFACE_ID = 1151
    const val ANCIENT_INTERFACE_ID = 12855

    data class TeleportBinding(
        val componentId: Int,
        val componentKey: String,
        val rawButtonIds: IntArray,
        val x: Int,
        val xRand: Int,
        val y: Int,
        val yRand: Int,
        val premium: Boolean,
    )

    private val loadedData by lazy { InterfaceMappingRegistry.magicData() }

    val spellbookToggleButtons: IntArray
        get() = loadedData.spellbookToggleButtons

    val autocastClearButtons: IntArray
        get() = loadedData.autocastClearButtons

    val autocastSelectButtons: IntArray
        get() = loadedData.autocastSelectButtons

    val autocastRefreshButtons: IntArray
        get() = loadedData.autocastRefreshButtons

    val teleports: List<TeleportBinding>
        get() = loadedData.teleports
}
