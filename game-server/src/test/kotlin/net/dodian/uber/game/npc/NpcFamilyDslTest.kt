package net.dodian.uber.game.npc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class NpcFamilyDslTest {
    @Test
    fun `primary spawn uses family primary id`() {
        val family = npcFamily("Test Rat", 100) {
            spawns {
                spawn(3200, 3201)
            }
        }

        assertEquals(100, family.spawns.single().npcId)
    }

    @Test
    fun `alternate spawn keeps explicit id`() {
        val family = npcFamily("Test Rat", 100) {
            ids(101)
            spawns {
                spawnId(101, 3200, 3201)
            }
        }

        assertEquals(101, family.spawns.single().npcId)
    }

    @Test
    fun `flat per spawn overrides survive into spawn definition`() {
        val family = npcFamily("Test Rat", 100) {
            spawns {
                spawn(
                    3200,
                    3201,
                    walkRadius = 3,
                    attack = 4,
                    defence = 5,
                    strength = 6,
                    hitpoints = 20,
                    ranged = 7,
                    magic = 8,
                    respawnTicks = 45,
                    attackAnimation = 111,
                    deathAnimation = 222,
                    attackRange = 2,
                    alwaysActive = true,
                    profile = profile("super-rat"),
                )
            }
        }

        val spawn = family.spawns.single()
        assertEquals(3, spawn.walkRadius)
        assertEquals(4, spawn.attack)
        assertEquals(5, spawn.defence)
        assertEquals(6, spawn.strength)
        assertEquals(20, spawn.hitpoints)
        assertEquals(7, spawn.ranged)
        assertEquals(8, spawn.magic)
        assertEquals(45, spawn.respawnTicks)
        assertEquals(111, spawn.attackAnimation)
        assertEquals(222, spawn.deathAnimation)
        assertEquals(2, spawn.attackRange)
        assertEquals(true, spawn.alwaysActive)
        assertEquals("super-rat", spawn.profile)
    }

    @Test
    fun `cache override builder drops placeholder cache values`() {
        val override = NpcCacheOverrideBuilder(100).apply {
            name = "Rat"
            examine = "no name"
            size = 1
        }.build()

        assertEquals("Rat", override.name)
        assertNull(override.examine)
        assertEquals(1, override.size)
    }

    @Test
    fun `runtime definition builder keeps server-applied values separate from cache overrides`() {
        val definition = NpcRuntimeDefinitionBuilder(100).apply {
            hitpoints = 20
            attack = 4
            attackAnimation = 111
            respawnTicks = 45
        }.build()

        assertEquals(20, definition.hitpoints)
        assertEquals(4, definition.attack)
        assertEquals(111, definition.attackAnimation)
        assertEquals(45, definition.respawnTicks)
    }
}
