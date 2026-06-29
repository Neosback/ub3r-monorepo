package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Ogre : NpcScript("Ogre", 136, 1153) {
    override val definition = define {
        stats {
            attack = 43
            attackAnimation = 806
            deathAnimation = 2304
            defence = 43
            examine = "A large dim looking humanoid."
            hitpoints = 60
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 43
        }

        spawns(
            spawn(3486, 9526, z = 2, profile = profile("ogre.soldiers")),
            spawn(3489, 9510, z = 2, profile = profile("ogre.soldiers")),
            spawn(2461, 3049, profile = profile("ogre.feldip_hills")),
            spawn(2463, 3044, profile = profile("ogre.feldip_hills")),
            spawn(2466, 3053, profile = profile("ogre.feldip_hills")),
            spawn(2469, 3045, profile = profile("ogre.feldip_hills")),
            spawn(2469, 3050, profile = profile("ogre.feldip_hills")),
            spawn(2480, 3046, profile = profile("ogre.feldip_hills")),
            spawn(2486, 3048, profile = profile("ogre.feldip_hills")),
            spawn(2604, 9441, profile = profile("ogre.wizards_guild")),
            spawn(3490, 9524, z = 2, profile = profile("ogre.guardians")),
            spawn(3494, 9509, z = 2, profile = profile("ogre.guardians")),
            spawn(3496, 9525, z = 2, profile = profile("ogre.guardians")),
            spawn(3498, 9511, z = 2, profile = profile("ogre.guardians")),
            spawn(3498, 9517, z = 2, profile = profile("ogre.guardians")),
            spawn(3501, 9523, z = 2, profile = profile("ogre.guardians")),
            spawn(3503, 9518, z = 2, profile = profile("ogre.guardians")),
            spawn(3505, 9526, z = 2, profile = profile("ogre.guardians")),
            spawn(3508, 9519, z = 2, profile = profile("ogre.guardians")),
            spawn(3509, 9524, z = 2, profile = profile("ogre.guardians")),
            spawn(2605, 9436, profile = profile("ogre.bronze_iron_steel_dragons")),
            spawn(2607, 9432, profile = profile("ogre.bronze_iron_steel_dragons")),
            spawn(2609, 9440, profile = profile("ogre.bronze_iron_steel_dragons")),
            spawn(2613, 9438, profile = profile("ogre.bronze_iron_steel_dragons")),
        )
    }
}
