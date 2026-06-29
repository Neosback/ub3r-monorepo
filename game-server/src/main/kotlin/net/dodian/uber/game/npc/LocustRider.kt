package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object LocustRider : NpcScript("LocustRider", 795, 796, 800, 801) {
    override val definition = define {
        stats {
            attack = 105
            attackAnimation = 5451
            deathAnimation = 5449
            defence = 50
            examine = "A mounted archer."
            hitpoints = 90
            magic = 1
            ranged = 90
            respawnTicks = 30
            strength = 90
        }

        spawns(
            spawn(3326, 2827, profile = profile("locust_rider.jaleustrophos_pyramid")),
            spawn(3328, 2824, profile = profile("locust_rider.jaleustrophos_pyramid")),
            spawn(3241, 2779, profile = profile("locust_rider.menaphos")),
            spawn(3237, 2780, profile = profile("locust_rider.menaphos")),
            spawn(3241, 2775, profile = profile("locust_rider.menaphos")),
            spawn(3237, 2775, profile = profile("locust_rider.menaphos")),
            spawn(3317, 2828, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3319, 2825, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3321, 2828, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3322, 2823, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3326, 2821, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3308, 2829, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3308, 2834, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3310, 2831, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3311, 2837, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3312, 2828, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3313, 2834, profile = profile("locust_rider.jalsavrah_pyramid")),
            spawn(3315, 2831, profile = profile("locust_rider.jalsavrah_pyramid")),
        )
    }
}
