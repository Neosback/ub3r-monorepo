package net.dodian.uber.game.engine.systems.interaction

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ObjectClipServiceDoorTest {
    private val position = Position(9000, 9000, 0)

    @AfterEach
    fun clearDoorClip() {
        ObjectClipService.clearForTests()
    }

    @Test
    fun `door clip remains solid while its face changes`() {
        val objectId = 199_900
        GameObjectData.addDefinition(
            GameObjectData(
                id = objectId,
                name = "Test door",
                description = "",
                sizeX = 1,
                sizeY = 1,
                solid = false,
                impenetrable = false,
                hasActionsFlag = true,
                decoration = false,
                walkType = 2,
            ),
        )

        ObjectClipService.applyDoor(position, objectId, face = -2)
        val closed = requireNotNull(ObjectClipService.getAppliedForTests(position))
        assertTrue(closed.solid)
        assertEquals(-2, closed.direction)

        ObjectClipService.applyDoor(position, objectId, face = -1)
        val open = requireNotNull(ObjectClipService.getAppliedForTests(position))
        assertTrue(open.solid)
        assertEquals(-1, open.direction)
    }
}
