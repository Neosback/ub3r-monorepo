package net.dodian.uber.game.engine.systems.cache

data class CacheSpotAnimDefinition(
    val id: Int,
    var modelId: Int = -1,
    var animationId: Int = -1,
    var resizeXY: Int = 128,
    var resizeZ: Int = 128,
    var rotation: Int = 0,
    var modelBrightness: Int = 0,
    var modelShadow: Int = 0,
    var originalModelColours: IntArray = IntArray(0),
    var modifiedModelColours: IntArray = IntArray(0),
)

object SpotAnimDefinitionDecoder {
    @JvmStatic
    fun decode(store: CacheStore): Map<Int, CacheSpotAnimDefinition> {
        val bytes = store.readArchiveFile(0, 2, "spotanim.dat") ?: error("Tarnish cache is missing spotanim.dat")
        val index = CacheBuffer(bytes)
        val highestId = index.readUnsignedShort()
        val definitions = LinkedHashMap<Int, CacheSpotAnimDefinition>()
        while (true) {
            val id = index.readUnsignedShort()
            if (id == 65535) break
            val length = index.readUnsignedShort()
            val definition = CacheSpotAnimDefinition(id)
            decodeOne(CacheBuffer(index.readBytes(length)), definition)
            definitions[id] = definition
            if (id >= highestId) break
        }
        return definitions
    }

    private fun decodeOne(buffer: CacheBuffer, definition: CacheSpotAnimDefinition) {
        while (true) {
            val opcode = buffer.readUnsignedByte()
            when (opcode) {
                0 -> break
                1 -> definition.modelId = buffer.readUnsignedShort()
                2 -> definition.animationId = buffer.readUnsignedShort()
                4 -> definition.resizeXY = buffer.readUnsignedShort()
                5 -> definition.resizeZ = buffer.readUnsignedShort()
                6 -> definition.rotation = buffer.readUnsignedShort()
                7 -> definition.modelBrightness = buffer.readUnsignedByte()
                8 -> definition.modelShadow = buffer.readUnsignedByte()
                40 -> {
                    val length = buffer.readUnsignedByte()
                    definition.originalModelColours = IntArray(length) { buffer.readUnsignedShort() }
                    definition.modifiedModelColours = IntArray(length) { buffer.readUnsignedShort() }
                }
                41 -> {
                    val length = buffer.readUnsignedByte()
                    repeat(length) {
                        buffer.readUnsignedShort()
                        buffer.readUnsignedShort()
                    }
                }
                else -> error("Unsupported spotanim.dat opcode $opcode for graphic ${definition.id}")
            }
        }
    }
}

object CacheSpotAnimDefinitions {
    @Volatile private var definitions: Map<Int, CacheSpotAnimDefinition> = emptyMap()
    fun replace(next: Map<Int, CacheSpotAnimDefinition>) { definitions = next.toMap() }
    @JvmStatic fun get(id: Int): CacheSpotAnimDefinition? = definitions[id]
    @JvmStatic fun size(): Int = definitions.size
}
