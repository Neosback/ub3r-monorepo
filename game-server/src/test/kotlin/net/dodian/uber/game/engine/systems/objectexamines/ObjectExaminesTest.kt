package net.dodian.uber.game.engine.systems.objectexamines

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ObjectExaminesTest {

    @Test
    fun `object examines load correctly from json`() {
        ObjectExamines.load()
        val crateExamine = ObjectExamines.getExamine(1)
        assertNotNull(crateExamine)
        assertEquals("Big mysterious crate. You wonder what could be inside.", crateExamine)
        
        val doorExamine = ObjectExamines.getExamine(3)
        assertNotNull(doorExamine)
        assertEquals("The door is closed.", doorExamine)
    }
}
