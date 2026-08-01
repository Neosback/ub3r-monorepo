package net.dodian.uber.game.engine.systems.combat

import net.dodian.uber.game.engine.config.gameWorldId
import net.dodian.uber.game.engine.event.GameEventScheduler
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Equipment
import net.dodian.uber.game.netty.listener.out.SendMessage
import net.dodian.uber.game.netty.listener.out.SendMoveComponent
import net.dodian.uber.game.netty.listener.out.SendSpecialAmount
import net.dodian.uber.game.netty.listener.out.SetInterfaceConfig

object CombatSpecialService {

    data class WeaponSpecial(
        val weaponIds: Set<Int>,
        val drainPercent: Int,
        val description: String,
    )

    private val weaponSpecials = listOf(
        WeaponSpecial(setOf(1215, 1231, 5680, 5698), 25, "Dragon dagger"),
        WeaponSpecial(setOf(11802, 81, 20368), 50, "Armadyl godsword"),
        WeaponSpecial(setOf(11804, 20370), 50, "Bandos godsword"),
        WeaponSpecial(setOf(11806, 20372), 50, "Saradomin godsword"),
        WeaponSpecial(setOf(11808, 20374), 50, "Zamorak godsword"),
        WeaponSpecial(setOf(1434), 25, "Dragon mace"),
        WeaponSpecial(setOf(4151, 80, 21371, 15441, 15442, 15443, 15444), 50, "Abyssal whip"),
        WeaponSpecial(setOf(4153, 24225), 50, "Granite maul"),
        WeaponSpecial(setOf(861, 12788), 55, "Magic shortbow"),
        WeaponSpecial(setOf(11235, 12765, 12766, 12767, 12768), 55, "Dark bow"),
        WeaponSpecial(setOf(13652, 20784), 50, "Dragon claws"),
        WeaponSpecial(setOf(1377), 100, "Dragon battleaxe"),
        WeaponSpecial(setOf(6739, 13241, 20011), 100, "Dragon axe"),
        WeaponSpecial(setOf(11920, 12797, 23677, 25376, 27695, 13243), 100, "Dragon pickaxe"),
        WeaponSpecial(setOf(11838, 12809), 100, "Saradomin sword"),
        WeaponSpecial(setOf(20849), 25, "Dragon thrownaxe"),
        WeaponSpecial(setOf(22804), 25, "Dragon knife"),
        WeaponSpecial(setOf(11791, 12904), 100, "Staff of the dead"),
        WeaponSpecial(setOf(21015), 50, "Dinh's bulwark"),
        WeaponSpecial(setOf(4587), 55, "Dragon scimitar"),
        WeaponSpecial(setOf(1305), 25, "Dragon longsword"),
        WeaponSpecial(setOf(13576), 50, "Dragon warhammer"),
        WeaponSpecial(setOf(1249, 5730), 25, "Dragon spear"),
        WeaponSpecial(setOf(7158), 60, "Dragon 2h sword"),
        WeaponSpecial(setOf(3204), 30, "Dragon halberd"),
        WeaponSpecial(setOf(12006), 50, "Abyssal tentacle"),
        WeaponSpecial(setOf(13263), 50, "Abyssal bludgeon"),
        WeaponSpecial(setOf(13265), 25, "Abyssal dagger"),
    )

    private val specialBarIds = mapOf(
        5855 to (7749 to 7761),
        4446 to (7649 to 7661),
        328 to (18566 to 18569),
        425 to (7474 to 7486),
        1698 to (7499 to 7511),
        1764 to (7549 to 7561),
        2276 to (7574 to 7586),
        2423 to (7599 to 7611),
        3796 to (7624 to 7636),
        4679 to (7674 to 7686),
        4705 to (7699 to 7711),
        5570 to (7724 to 7736),
        8460 to (8493 to 8505),
        12290 to (12323 to 12335),
    )

    @JvmStatic
    fun getSpecialForWeapon(weaponId: Int): WeaponSpecial? =
        weaponSpecials.firstOrNull { weaponId in it.weaponIds }

    @JvmStatic
    fun hasSpecialWeapon(player: Client): Boolean =
        getSpecialForWeapon(player.equipment[Equipment.Slot.WEAPON.id]) != null

    @JvmStatic
    fun toggleSpecial(player: Client) {
        if (gameWorldId != 2) {
            player.send(SendMessage("Special attacks are currently in beta and not available on this world."))
            return
        }

        if (player.duelFight && player.duelRule[3]) {
            player.send(SendMessage("Special attacks have been disabled in this duel."))
            return
        }

        val special = getSpecialForWeapon(player.equipment[Equipment.Slot.WEAPON.id]) ?: run {
            player.send(SendMessage("Your weapon does not have a special attack."))
            return
        }

        if (player.specialActivated) {
            player.specialActivated = false
            player.varbit(301, 0)
        } else if (player.specialAmount < special.drainPercent) {
            player.send(SendMessage("You do not have enough special energy left! (${player.specialAmount}%)"))
        } else {
            player.specialActivated = true
            player.varbit(301, 1)
        }
    }

    @JvmStatic
    fun drainSpecial(player: Client) {
        if (!player.specialActivated) return
        val special = getSpecialForWeapon(player.equipment[Equipment.Slot.WEAPON.id]) ?: return
        player.specialAmount = (player.specialAmount - special.drainPercent).coerceAtLeast(0)
        player.specialActivated = false
        player.varbit(301, 0)
        player.send(SendSpecialAmount(player.specialAmount))
        updateSpecialBar(player)
    }

    @JvmStatic
    fun onWeaponEquip(player: Client) {
        player.specialActivated = false
        player.varbit(301, 0)
        val currentInterface = player.currentCombatInterface
        val barIds = specialBarIds[currentInterface]
        val hasSpecial = getSpecialForWeapon(player.equipment[Equipment.Slot.WEAPON.id]) != null
        if (barIds != null && hasSpecial) {
            player.send(SetInterfaceConfig(0, barIds.first))
            player.send(SendSpecialAmount(player.specialAmount))
            updateSpecialBar(player)
        } else if (barIds != null) {
            player.send(SetInterfaceConfig(1, barIds.first))
            player.send(SendSpecialAmount(player.specialAmount))
        }
    }

    @JvmStatic
    fun updateSpecialBar(player: Client) {
        val currentInterface = player.currentCombatInterface
        val barIds = specialBarIds[currentInterface] ?: return
        val specialMeter = barIds.second
        val filled = player.specialAmount / 10
        var meter = specialMeter
        for (i in 10 downTo 1) {
            val x = if (filled >= i) 500 else 0
            meter--
            player.send(SendMoveComponent(x, 0, meter))
        }
    }

    @JvmStatic
    fun restoreSpecial(player: Client, amount: Int) {
        player.specialAmount = (player.specialAmount + amount).coerceAtMost(100)
        player.send(SendSpecialAmount(player.specialAmount))
        updateSpecialBar(player)
    }

    init {
        GameEventScheduler.runRepeatingMs(60_000) {
            val players = net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players
            for (i in 0 until players.size) {
                val player = players[i] as? Client ?: continue
                if (player.specialAmount < 100) {
                    restoreSpecial(player, 10)
                }
            }
            true
        }
    }
}
