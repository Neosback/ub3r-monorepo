@file:Suppress("unused")

package net.dodian.uber.game.ui

import net.dodian.uber.game.ui.buttons.InterfaceButtonContent
import net.dodian.uber.game.ui.buttons.buttonBinding
import net.dodian.uber.game.netty.listener.out.ShowInterface
import net.dodian.uber.game.netty.listener.out.SendScreenMode

object SettingsInterface : InterfaceButtonContent {
    private const val SETTINGS_TAB_ID = 44500
    private const val MORE_SETTINGS_TAB_ID = 23000

    // 44511 and 23020 were old client buttons; no longer present in new client.
    // 50040 is "Advanced options" toggle within ROOT 50020 — handled client-side, server acks only.
    private val advancedOptionsAckButtons = intArrayOf(50040)
    private val pinHelpButtons = intArrayOf(58073)
    private val bossYellEnableButtons = intArrayOf(24136)
    private val bossYellDisableButtons = intArrayOf(24137)

    // New Tabbed Settings Buttons
    private val settingsTabMain = intArrayOf(50002)
    private val settingsTabAudio = intArrayOf(50003)
    private val settingsTabKeybinds = intArrayOf(50005)
    private val settingsTabDonator = intArrayOf(50008)

    private val clientModeFixed = intArrayOf(50031)
    private val clientModeResizable = intArrayOf(50034)
    private val clientModeFullscreen = intArrayOf(50037)

    private val keybindingOpen = intArrayOf(50203)

    override val bindings =
        listOf(
            buttonBinding(-1, 0, "settings.advanced_options_ack", advancedOptionsAckButtons) { client, _ ->
                client.send(ShowInterface(28500))
                true
            },
            buttonBinding(-1, 2, "settings.pin_help", pinHelpButtons) { client, _ ->
                client.sendMessage("Visit the Dodian.net UserCP and click edit pin to remove your pin")
                true
            },
            buttonBinding(-1, 3, "settings.boss_yell.enable", bossYellEnableButtons) { client, _ ->
                client.yellOn = true
                client.sendMessage("You enabled the boss yell messages.")
                true
            },
            buttonBinding(-1, 4, "settings.boss_yell.disable", bossYellDisableButtons) { client, _ ->
                client.yellOn = false
                client.sendMessage("You disabled the boss yell messages.")
                true
            },
            // Tab Switch Bindings
            buttonBinding(-1, 5, "settings.tab.main", settingsTabMain) { client, _ ->
                client.setSidebarInterface(11, 50020)
                client.varbit(980, 0)
                true
            },
            buttonBinding(-1, 6, "settings.tab.audio", settingsTabAudio) { client, _ ->
                client.setSidebarInterface(11, 50100)
                client.varbit(980, 1)
                true
            },
            buttonBinding(-1, 8, "settings.tab.keybinds", settingsTabKeybinds) { client, _ ->
                client.sendMessage(":settingupdate:")
                client.setSidebarInterface(11, 50300)
                client.varbit(980, 3)
                true
            },
            buttonBinding(-1, 9, "settings.tab.donator", settingsTabDonator) { client, _ ->
                client.setSidebarInterface(11, 50400)
                client.varbit(980, 4)
                true
            },
            // Client Screen Mode Bindings
            buttonBinding(-1, 10, "settings.client_mode.fixed", clientModeFixed) { client, _ ->
                client.send(SendScreenMode(765, 503))
                true
            },
            buttonBinding(-1, 11, "settings.client_mode.resizable", clientModeResizable) { client, _ ->
                client.send(SendScreenMode(766, 559))
                true
            },
            buttonBinding(-1, 12, "settings.client_mode.fullscreen", clientModeFullscreen) { client, _ ->
                client.send(SendScreenMode(-1, -1))
                true
            },
            // Keybinding Bindings
            buttonBinding(-1, 14, "settings.keybinding.open", keybindingOpen) { client, _ ->
                client.sendMessage(":keybinding:")
                client.send(ShowInterface(39300))
                true
            }
        )
}