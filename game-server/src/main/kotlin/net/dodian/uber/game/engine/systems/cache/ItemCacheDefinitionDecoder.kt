package net.dodian.uber.game.engine.systems.cache

data class CacheItemDefinition(
    val id: Int,
    var name: String = "",
    var description: String = "",
    var stackable: Boolean = false,
    var tradeable: Boolean = false,
    var members: Boolean = false,
    var value: Int = 1,
    var notedId: Int = -1,
    var noteTemplateId: Int = -1,
)

object ItemCacheDefinitionDecoder {
    @JvmStatic
    fun decode(store: CacheStore): Map<Int, CacheItemDefinition> {
        val dat = store.readArchiveFile(0, 2, "obj.dat") ?: error("Tarnish cache is missing obj.dat")
        val idx = store.readArchiveFile(0, 2, "obj.idx") ?: error("Tarnish cache is missing obj.idx")
        val index = CacheBuffer(idx)
        val count = index.readUnsignedShort()
        val offsets = IntArray(count)
        var offset = 2
        repeat(count) { id -> offsets[id] = offset; offset += index.readUnsignedShort() }
        val data = CacheBuffer(dat)
        return (0 until count).associateWithTo(LinkedHashMap()) { id ->
            data.seek(offsets[id])
            decodeOne(id, data)
        }
    }

    private fun decodeOne(id: Int, data: CacheBuffer): CacheItemDefinition {
        val definition = CacheItemDefinition(id)
        var lastOpcode = -1
        while (true) {
            val opcode = data.readUnsignedByte()
            when {
                opcode == 0 -> return definition
                opcode == 1 -> data.skip(2)
                opcode == 2 -> definition.name = data.readStringNullTerminated()
                opcode == 3 -> definition.description = data.readStringNullTerminated()
                opcode in 4..6 -> data.skip(2)
                opcode == 7 || opcode == 8 -> data.readUnsignedShort()
                opcode == 9 -> data.readStringNullTerminated()
                opcode == 10 -> data.skip(2)
                opcode == 11 -> definition.stackable = true
                opcode == 12 -> definition.value = data.readInt()
                opcode == 13 || opcode == 14 || opcode == 27 -> data.skip(1)
                opcode == 16 -> definition.members = true
                opcode == 23 || opcode == 25 -> data.skip(3)
                opcode == 24 || opcode == 26 -> data.skip(2)
                opcode in 30..39 -> data.readStringNullTerminated()
                opcode == 40 || opcode == 41 -> repeat(data.readUnsignedByte()) { data.skip(4) }
                opcode == 42 -> data.skip(1)
                opcode == 65 -> definition.tradeable = true
                opcode == 75 -> data.skip(2)
                opcode == 78 || opcode == 79 || opcode in 90..95 -> data.skip(2)
                opcode == 97 -> definition.notedId = data.readUnsignedShort()
                opcode == 98 -> definition.noteTemplateId = data.readUnsignedShort()
                opcode in 100..109 -> data.skip(4)
                opcode in 110..112 -> data.skip(2)
                opcode == 113 || opcode == 114 || opcode == 115 -> data.skip(1)
                opcode == 139 || opcode == 140 || opcode == 148 || opcode == 149 -> data.skip(2)
                opcode == 249 -> repeat(data.readUnsignedByte()) {
                    val stringValue = data.readUnsignedByte() == 1
                    data.readMedium()
                    if (stringValue) data.readStringNullTerminated() else data.readInt()
                }
                else -> error("Unsupported obj.dat opcode $opcode for item $id (last=$lastOpcode)")
            }
            lastOpcode = opcode
        }
    }
}