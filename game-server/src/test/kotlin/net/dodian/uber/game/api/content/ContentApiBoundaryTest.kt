package net.dodian.uber.game.api.content

import net.dodian.uber.game.api.plugin.skills.SkillPlayer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ContentApiBoundaryTest {
    @Test
    fun `public content player contract has no protocol player type`() {
        val clientTypeName = "net.dodian.uber.game.model.entity.player.Client"
        val publicTypes = listOf(ContentPlayer::class.java, SkillPlayer::class.java)

        publicTypes.forEach { type ->
            assertFalse(type.declaredMethods.any { method ->
                method.returnType.name == clientTypeName || method.parameterTypes.any { it.name == clientTypeName }
            }, "${type.name} must not expose Client")
        }
    }

    @Test
    fun `client skill adapter remains engine internal`() {
        val adapter = Class.forName("net.dodian.uber.game.engine.systems.skills.ClientSkillPlayerAdapter")
        assertTrue(adapter.name.contains(".engine.systems.skills."))
    }
}
