package net.dodian.uber.game.engine.systems.cache

data class CacheVarbitDefinition(
    val id: Int,
    val varp: Int,
    val leastSignificantBit: Int,
    val mostSignificantBit: Int,
) {
    val bitCount: Int get() = (mostSignificantBit - leastSignificantBit + 1).coerceAtLeast(0)
    val maxValue: Int get() = if (bitCount <= 0) 0 else (1 shl bitCount) - 1
}

object VarbitDefinitionDecoder {
    @JvmStatic
    fun decode(store: CacheStore): Map<Int, CacheVarbitDefinition> {
        val bytes = store.readArchiveFile(0, 2, "varbit.dat") ?: return emptyMap()
        val data = CacheBuffer(bytes)
        val count = data.readUnsignedShort()
        val definitions = LinkedHashMap<Int, CacheVarbitDefinition>(count)
        repeat(count) {
            val id = data.readUnsignedShort()
            if (id == 65535) return@repeat
            val length = data.readUnsignedShort()
            val payload = CacheBuffer(data.readBytes(length))
            readDefinition(id, payload)?.let { definitions[id] = it }
        }
        return definitions
    }

    private fun readDefinition(id: Int, data: CacheBuffer): CacheVarbitDefinition? {
        var varp = -1
        var least = 0
        var most = 0
        while (true) {
            when (val opcode = data.readUnsignedByte()) {
                0 -> break
                1 -> {
                    varp = data.readUnsignedShort()
                    least = data.readUnsignedByte()
                    most = data.readUnsignedByte()
                }
                else -> error("Unsupported varbit.dat opcode $opcode for varbit $id")
            }
        }
        return if (varp >= 0) CacheVarbitDefinition(id, varp, least, most) else null
    }
}
