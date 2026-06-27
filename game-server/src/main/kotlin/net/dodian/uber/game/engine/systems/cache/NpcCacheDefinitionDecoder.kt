package net.dodian.uber.game.engine.systems.cache

data class CacheNpcDefinition(
    val id: Int,
    var name: String = "",
    var examine: String = "",
    var size: Int = 1,
    var combatLevel: Int = 0,
    var standingAnimation: Int = -1,
    var walkingAnimation: Int = -1,
    var halfTurnAnimation: Int = -1,
    var clockwiseTurnAnimation: Int = -1,
    var anticlockwiseTurnAnimation: Int = -1,
    var actions: Array<String?> = arrayOfNulls(5),
    var attackAnimation: Int = 806,
    var deathAnimation: Int = 836,
    var respawnTicks: Int = 60,
    var attack: Int = 0,
    var strength: Int = 0,
    var defence: Int = 0,
    var hitpoints: Int = 0,
    var ranged: Int = 0,
    var magic: Int = 0,
)

object NpcCacheDefinitionDecoder {
    @JvmStatic
    fun decode(store: CacheStore): Map<Int, CacheNpcDefinition> {
        val dat = store.readArchiveFile(0, 2, "npc.dat") ?: error("Tarnish cache is missing npc.dat")
        val idx = store.readArchiveFile(0, 2, "npc.idx") ?: error("Tarnish cache is missing npc.idx")
        val index = CacheBuffer(idx)
        val count = index.readUnsignedShort()
        val offsets = IntArray(count)
        var offset = 0
        for (id in 0 until count) {
            val size = index.readUnsignedShort()
            offsets[id] = offset
            offset += size
        }

        val data = CacheBuffer(dat)
        val definitions = LinkedHashMap<Int, CacheNpcDefinition>(count)
        for (id in 0 until count) {
            data.seek(offsets[id])
            definitions[id] = decodeOne(id, data)
        }
        return definitions
    }

    private fun decodeOne(id: Int, data: CacheBuffer): CacheNpcDefinition {
        val definition = CacheNpcDefinition(id)
        var lastOpcode = -1
        while (true) {
            val opcode = data.readUnsignedByte()
            when (opcode) {
                0 -> return definition
                1 -> repeat(data.readUnsignedByte()) { data.skip(2) }
                2 -> definition.name = data.readStringNullTerminated()
                3 -> definition.examine = data.readJString()
                12 -> definition.size = data.readByte()
                13 -> definition.standingAnimation = data.readUnsignedShort()
                14 -> definition.walkingAnimation = data.readUnsignedShort()
                15, 16, 18 -> data.skip(2)
                17 -> {
                    definition.walkingAnimation = data.readUnsignedShort()
                    definition.halfTurnAnimation = animationOrWalk(data.readUnsignedShort(), definition.walkingAnimation)
                    definition.clockwiseTurnAnimation = animationOrWalk(data.readUnsignedShort(), definition.walkingAnimation)
                    definition.anticlockwiseTurnAnimation = animationOrWalk(data.readUnsignedShort(), definition.walkingAnimation)
                }
                in 30..34 -> {
                    val action = data.readStringNullTerminated()
                    definition.actions[opcode - 30] = action.takeUnless { it.equals("hidden", true) }
                }
                40, 41 -> repeat(data.readUnsignedByte()) { data.skip(4) }
                60 -> repeat(data.readUnsignedByte()) { data.skip(2) }
                61, 62 -> repeat(data.readUnsignedByte()) { data.skip(4) }
                74, 75, 76, 77, 78, 79 -> data.skip(2)
                90, 91, 92 -> data.skip(2)
                93, 99, 107, 109, 111, 122, 123, 129, 130, 145, 147, 189 -> Unit
                95 -> definition.combatLevel = data.readUnsignedShort()
                97, 98, 103, 114, 116, 124, 126, 146 -> data.skip(2)
                100, 101 -> data.skip(1)
                102 -> {
                    val bitfield = data.readUnsignedByte()
                    val length = if (bitfield == 0) 0 else 32 - Integer.numberOfLeadingZeros(bitfield)
                    repeat(length) { index ->
                        if (bitfield and (1 shl index) != 0) {
                            data.readBigSmart2()
                            data.readUnsignedShortSmartMinusOne()
                        }
                    }
                }
                106 -> skipTransforms(data, hasFallback = false)
                115, 117 -> data.skip(8)
                118 -> skipTransforms(data, hasFallback = true)
                249 -> repeat(data.readUnsignedByte()) {
                    val stringValue = data.readUnsignedByte() == 1
                    data.readMedium()
                    if (stringValue) data.readStringNullTerminated() else data.readInt()
                }
                else -> {
                    System.err.println("Unsupported npc.dat opcode $opcode for NPC $id (last=$lastOpcode)")
                    return definition
                }
            }
            lastOpcode = opcode
        }
    }

    private fun skipTransforms(data: CacheBuffer, hasFallback: Boolean) {
        data.skip(4)
        if (hasFallback) data.skip(2)
        val count = data.readUnsignedByte()
        repeat(count + 1) { data.skip(2) }
    }

    private fun animationOrWalk(value: Int, walk: Int): Int = if (value == 65535) walk else value
}