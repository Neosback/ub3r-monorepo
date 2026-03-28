package net.dodian.uber.game.content.interfaces.settings

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object SettingsComponents {
    const val SETTINGS_TAB_ID = 44500
    const val MORE_SETTINGS_TAB_ID = 23000

    val openMoreSettingsButtons: IntArray
        get() = InterfaceMappingRegistry.settingsData().openMoreSettingsButtons
    val closeMoreSettingsButtons: IntArray
        get() = InterfaceMappingRegistry.settingsData().closeMoreSettingsButtons
    val pinHelpButtons: IntArray
        get() = InterfaceMappingRegistry.settingsData().pinHelpButtons
    val bossYellEnableButtons: IntArray
        get() = InterfaceMappingRegistry.settingsData().bossYellEnableButtons
    val bossYellDisableButtons: IntArray
        get() = InterfaceMappingRegistry.settingsData().bossYellDisableButtons
}
