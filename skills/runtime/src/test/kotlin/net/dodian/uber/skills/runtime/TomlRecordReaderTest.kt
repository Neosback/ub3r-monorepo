package net.dodian.uber.skills.runtime

import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TomlRecordReaderTest {
    @Test
    fun `missing resources fail with the resource path`() {
        val error = assertThrows(IllegalStateException::class.java) {
            TomlRecordReader.readRecords("missing/skill.toml", "record")
        }
        assertEquals(true, error.message!!.contains("missing/skill.toml"))
    }

    @Test
    fun `malformed toml fails instead of silently dropping records`() {
        val loader = object : ClassLoader() {
            override fun getResourceAsStream(name: String) =
                if (name == "broken.toml") ByteArrayInputStream("[[record]\\nvalue = [".toByteArray()) else null
        }
        assertThrows(IllegalStateException::class.java) {
            TomlRecordReader.readRecords("broken.toml", "record", loader)
        }
    }
}
