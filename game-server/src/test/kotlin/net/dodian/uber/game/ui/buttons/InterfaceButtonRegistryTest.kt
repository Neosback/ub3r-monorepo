package net.dodian.uber.game.ui.buttons

import net.dodian.uber.game.model.entity.player.Client
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InterfaceButtonRegistryTest {

    @Test
    fun `bootstrap succeeds and lookup building works without negative bounds crash`() {
        // Calling bootstrap should run without throwing any ArrayIndexOutOfBoundsException
        InterfaceButtonRegistry.bootstrap()
    }

    @Test
    fun `resolve handles negative and positive short-overflowed button IDs correctly`() {
        InterfaceButtonRegistry.bootstrap()
        
        // Create a dummy client
        val client = Client(null, 1)
        client.activeInterfaceId = -1

        // 56507 is registered. Let's see if resolve finds it for both positive 56507 and negative -9029
        val bindingPos = InterfaceButtonRegistry.resolve(client, 56507, -1)
        assertNotNull(bindingPos)
        assertEquals("exp_counter.settings.pos_top", bindingPos?.componentKey)

        val bindingNeg = InterfaceButtonRegistry.resolve(client, -9029, -1)
        assertNotNull(bindingNeg)
        assertEquals("exp_counter.settings.pos_top", bindingNeg?.componentKey)

        // Make sure positive and negative match the same binding
        assertEquals(bindingPos, bindingNeg)
    }

    @Test
    fun `resolve returns null for unregistered button IDs`() {
        InterfaceButtonRegistry.bootstrap()
        val client = Client(null, 1)
        assertNull(InterfaceButtonRegistry.resolve(client, 999999, -1))
        assertNull(InterfaceButtonRegistry.resolve(client, -999999, -1))
    }
}