package net.dodian.uber.game.engine.systems.cache

import kotlin.math.abs

data class CacheAnimationDefinition(
    val id: Int,
    var length: Int = 0,
    var primary: IntArray = IntArray(0),
    var duration: IntArray = IntArray(0),
    var padding: Int = -1,
    var interleaveOrder: IntArray? = null,
    var allowsRotation: Boolean = false,
    var priority: Int = 5,
    var shield: Int = -1,
    var weapon: Int = -1,
    var resetCycle: Int = 99,
    var runFlag: Int = -1,
    var walkFlag: Int = -1,
    var type: Int = 1,
) {
    val durationMillis: Int get() = duration.sumOf { abs(it) * 20 }
}

object AnimationDefinitionDecoder {
    @JvmStatic
    fun decode(store: CacheStore): Map<Int, CacheAnimationDefinition> {
        val bytes = store.readArchiveFile(0, 2, "seq.dat") ?: error("Tarnish cache is missing seq.dat")
        val index = CacheBuffer(bytes)
        val highestId = index.readUnsignedShort()
        val definitions = LinkedHashMap<Int, CacheAnimationDefinition>()
        while (true) {
            val id = index.readUnsignedShort()
            if (id == 65535) break
            val length = index.readUnsignedShort()
            val definition = CacheAnimationDefinition(id)
            decodeOne(CacheBuffer(index.readBytes(length)), definition)
            definitions[id] = definition
            if (id >= highestId) break
        }
        return definitions
    }

    private fun decodeOne(buffer: CacheBuffer, definition: CacheAnimationDefinition) {
        while (true) {
            val opcode = buffer.readUnsignedByte()
            when (opcode) {
                0 -> break
                1 -> {
                    definition.length = buffer.readUnsignedShort()
                    definition.duration = IntArray(definition.length) { buffer.readUnsignedShort() }
                    definition.primary = IntArray(definition.length) { buffer.readUnsignedShort() }
                    repeat(definition.length) { definition.primary[it] += buffer.readUnsignedShort() shl 16 }
                }
                2 -> definition.padding = buffer.readUnsignedShort()
                3 -> definition.interleaveOrder = IntArray(buffer.readUnsignedByte() + 1).also { values ->
                    repeat(values.size - 1) { values[it] = buffer.readUnsignedByte() }
                    values[values.lastIndex] = 9_999_999
                }
                4 -> definition.allowsRotation = true
                5 -> definition.priority = buffer.readUnsignedByte()
                6 -> definition.shield = buffer.readUnsignedShort()
                7 -> definition.weapon = buffer.readUnsignedShort()
                8 -> definition.resetCycle = buffer.readUnsignedByte()
                9 -> definition.runFlag = buffer.readUnsignedByte()
                10 -> definition.walkFlag = buffer.readUnsignedByte()
                11 -> definition.type = buffer.readUnsignedByte()
                12 -> {
                    val length = buffer.readUnsignedByte()
                    repeat(length) { buffer.readUnsignedShort() }
                    repeat(length) { buffer.readUnsignedShort() }
                }
                13 -> repeat(buffer.readUnsignedByte()) { buffer.readMedium() }
                14 -> buffer.readInt()
                15 -> repeat(buffer.readUnsignedShort()) { buffer.readUnsignedShort(); buffer.readMedium() }
                16 -> { buffer.readUnsignedShort(); buffer.readUnsignedShort() }
                17 -> repeat(buffer.readUnsignedByte()) { buffer.readUnsignedByte() }
                else -> error("Unsupported seq.dat opcode $opcode for animation ${definition.id}")
            }
            if (opcode == 0) break
        }
        if (definition.length == 0) {
            definition.length = 1
            definition.primary = intArrayOf(-1)
            definition.duration = intArrayOf(-1)
        }
        if (definition.runFlag == -1) definition.runFlag = if (definition.interleaveOrder == null) 0 else 2
        if (definition.priority == -1) definition.priority = if (definition.interleaveOrder == null) 0 else 2
    }
}

object CacheAnimationDefinitions {
    @Volatile private var definitions: Map<Int, CacheAnimationDefinition> = emptyMap()
    fun replace(next: Map<Int, CacheAnimationDefinition>) { definitions = next.toMap() }
    @JvmStatic fun get(id: Int): CacheAnimationDefinition? = definitions[id]
    @JvmStatic fun size(): Int = definitions.size
}