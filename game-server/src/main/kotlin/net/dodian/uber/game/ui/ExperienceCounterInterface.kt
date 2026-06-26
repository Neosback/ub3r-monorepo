package net.dodian.uber.game.ui

import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.netty.listener.out.RemoveInterfaces
import net.dodian.uber.game.netty.listener.out.SendExpCounter
import net.dodian.uber.game.netty.listener.out.SendExpCounterSetting
import net.dodian.uber.game.netty.listener.out.SendMessage
import net.dodian.uber.game.netty.listener.out.SetVarbit
import net.dodian.uber.game.netty.listener.out.ShowInterface
import net.dodian.uber.game.ui.buttons.InterfaceButtonContent
import net.dodian.uber.game.ui.buttons.buttonBinding

object ExperienceCounterInterface : InterfaceButtonContent {
    override val bindings = listOf(
        // Toggle/Settings EXP button
        buttonBinding(-1, 0, "exp_counter.toggle_settings", intArrayOf(475)) { client, _ ->
            client.send(SendExpCounterSetting(0, 0))
            client.send(ShowInterface(56500))
            true
        },
        // Reset EXP counter
        buttonBinding(-1, 1, "exp_counter.reset", intArrayOf(476)) { client, _ ->
            DialogueService.start(client) {
                statement("Are you sure you want to do this?", "This action cannot be undone!")
                options(
                    "Select an Option",
                    DialogueOption("Reset EXP counter") {
                        action { c ->
                            c.send(SendExpCounter(0))
                            c.send(SendMessage("Your experience has been reset."))
                            c.send(RemoveInterfaces())
                        }
                    },
                    DialogueOption("Nevermind") {
                        action { c ->
                            c.send(RemoveInterfaces())
                        }
                    }
                )
            }
            true
        },
        // Dialogue option to Lock/Unlock Experience
        buttonBinding(-1, 2, "exp_counter.lock_toggle", intArrayOf(477)) { client, _ ->
            DialogueService.start(client) {
                options(
                    "Select an Option",
                    DialogueOption(if (client.lockExperience) "Unlock experience" else "Lock experience") {
                        action { c ->
                            c.lockExperience = !c.lockExperience
                            c.send(SendMessage("Your experience is now " + (if (c.lockExperience) "locked." else "un-locked.")))
                            c.send(RemoveInterfaces())
                        }
                    },
                    DialogueOption("Nevermind") {
                        action { c ->
                            c.send(RemoveInterfaces())
                        }
                    }
                )
            }
            true
        },
        // EXP Settings: Counter Position Top/Middle/Bottom
        buttonBinding(-1, 3, "exp_counter.settings.pos_top", intArrayOf(56507, 56510)) { client, _ ->
            client.send(SendExpCounterSetting(2, 0))
            true
        },
        buttonBinding(-1, 4, "exp_counter.settings.pos_mid", intArrayOf(56508, 56511)) { client, _ ->
            client.send(SendExpCounterSetting(2, 1))
            true
        },
        buttonBinding(-1, 5, "exp_counter.settings.pos_bot", intArrayOf(56509, 56512)) { client, _ ->
            client.send(SendExpCounterSetting(2, 2))
            true
        },
        // EXP Settings: Counter Size Small/Medium/Large
        buttonBinding(-1, 6, "exp_counter.settings.size_small", intArrayOf(56514, 56517)) { client, _ ->
            client.send(SendExpCounterSetting(3, 0))
            true
        },
        buttonBinding(-1, 7, "exp_counter.settings.size_med", intArrayOf(56515, 56518)) { client, _ ->
            client.send(SendExpCounterSetting(3, 1))
            true
        },
        buttonBinding(-1, 8, "exp_counter.settings.size_large", intArrayOf(56516, 56519)) { client, _ ->
            client.send(SendExpCounterSetting(3, 2))
            true
        },
        // EXP Settings: Counter Style Normal/Grouped
        buttonBinding(-1, 9, "exp_counter.settings.style_normal", intArrayOf(56548, 56550)) { client, _ ->
            client.send(SendExpCounterSetting(5, 0))
            true
        },
        buttonBinding(-1, 10, "exp_counter.settings.style_grouped", intArrayOf(56549, 56551)) { client, _ ->
            client.send(SendExpCounterSetting(5, 1))
            true
        },
        // EXP Settings: Colors
        buttonBinding(-1, 11, "exp_counter.settings.color_white", intArrayOf(56521, 56527)) { client, _ ->
            client.send(SetVarbit(772, 0))
            client.send(SendExpCounterSetting(1, 0xFFFFFF))
            true
        },
        buttonBinding(-1, 12, "exp_counter.settings.color_cyan", intArrayOf(56522, 56528)) { client, _ ->
            client.send(SetVarbit(772, 1))
            client.send(SendExpCounterSetting(1, 0x00FFFF))
            true
        },
        buttonBinding(-1, 13, "exp_counter.settings.color_purple", intArrayOf(56523, 56529)) { client, _ ->
            client.send(SetVarbit(772, 2))
            client.send(SendExpCounterSetting(1, 0xCD7EF2))
            true
        },
        buttonBinding(-1, 14, "exp_counter.settings.color_green", intArrayOf(56524, 56530)) { client, _ ->
            client.send(SetVarbit(772, 3))
            client.send(SendExpCounterSetting(1, 0x3BF576))
            true
        },
        buttonBinding(-1, 15, "exp_counter.settings.color_orange", intArrayOf(56525, 56531)) { client, _ ->
            client.send(SetVarbit(772, 4))
            client.send(SendExpCounterSetting(1, 0xFC7312))
            true
        },
        buttonBinding(-1, 16, "exp_counter.settings.color_red", intArrayOf(56526, 56532)) { client, _ ->
            client.send(SetVarbit(772, 5))
            client.send(SendExpCounterSetting(1, 0xDE2352))
            true
        },
        // EXP Settings: Lock/Unlock Button Config
        buttonBinding(-1, 17, "exp_counter.settings.lock", intArrayOf(56538, 56540)) { client, _ ->
            client.lockExperience = true
            client.send(SetVarbit(775, 0))
            client.send(SendMessage("Your experience is now locked."))
            true
        },
        buttonBinding(-1, 18, "exp_counter.settings.unlock", intArrayOf(56539, 56541)) { client, _ ->
            client.lockExperience = false
            client.send(SetVarbit(775, 1))
            client.send(SendMessage("Your experience is now un-locked."))
            true
        },
        // EXP Settings: Experience Rates
        buttonBinding(-1, 19, "exp_counter.settings.rate_normal", intArrayOf(56543, 56545)) { client, _ ->
            client.send(SetVarbit(776, 0))
            client.send(SendMessage("Your experience rate is now normal."))
            true
        },
        buttonBinding(-1, 20, "exp_counter.settings.rate_runescape", intArrayOf(56544, 56546)) { client, _ ->
            client.send(SetVarbit(776, 1))
            client.send(SendMessage("Your experience rate is now Runescape."))
            true
        }
    )
}