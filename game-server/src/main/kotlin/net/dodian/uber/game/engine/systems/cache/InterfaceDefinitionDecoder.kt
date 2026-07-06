package net.dodian.uber.game.engine.systems.cache

data class CacheInterfaceDefinition(
    val id: Int,
    val parentId: Int,
    var type: Int = 0,
    var atActionType: Int = 0,
    var contentType: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var opacity: Byte = 0,
    var hoverType: Int = -1,
    var valueCompareType: IntArray? = null,
    var requiredValues: IntArray? = null,
    var scripts: Array<IntArray>? = null,
    var scrollMax: Int = 0,
    var isMouseoverTriggered: Boolean = false,
    var children: IntArray? = null,
    var childX: IntArray? = null,
    var childY: IntArray? = null,
    var inv: IntArray? = null,
    var invStackSizes: IntArray? = null,
    var invReplace: Boolean = false,
    var isInventoryInterface: Boolean = false,
    var usableItemInterface: Boolean = false,
    var invStackable: Boolean = false,
    var invSpritePadX: Int = 0,
    var invSpritePadY: Int = 0,
    var spritesX: IntArray? = null,
    var spritesY: IntArray? = null,
    var sprites: Array<String>? = null,
    var actions: Array<String?>? = null,
    var isFilled: Boolean = false,
    var centerText: Boolean = false,
    var textShadow: Boolean = false,
    var disabledMessage: String = "",
    var enabledMessage: String = "",
    var textColor: Int = 0,
    var anInt219: Int = 0,
    var textHoverColor: Int = 0,
    var anInt239: Int = 0,
    var disabledSprite: String = "",
    var enabledSprite: String = "",
    var defaultMediaType: Int = 0,
    var mediaID: Int = 0,
    var anInt255: Int = 0,
    var anInt256: Int = 0,
    var anInt257: Int = -1,
    var anInt258: Int = -1,
    var modelZoom: Int = 0,
    var modelRotation1: Int = 0,
    var modelRotation2: Int = 0,
    var selectedActionName: String = "",
    var spellName: String = "",
    var spellUsableOn: Int = 0,
    var tooltip: String = "",
)

object InterfaceDefinitionDecoder {
    @JvmStatic
    fun decode(store: CacheStore): Map<Int, CacheInterfaceDefinition> {
        val bytes = store.readArchiveFile(0, 3, "data") ?: return emptyMap()
        val data = CacheBuffer(bytes)
        val definitions = LinkedHashMap<Int, CacheInterfaceDefinition>()
        
        // Skip first unsigned short as in RSInterface.java
        data.readUnsignedShort()
        
        var currentParentId = -1
        while (data.position < bytes.size) {
            var id = data.readUnsignedShort()
            if (id == 65535) {
                currentParentId = data.readUnsignedShort()
                id = data.readUnsignedShort()
            }
            val definition = CacheInterfaceDefinition(id, currentParentId)
            decodeOne(data, definition)
            definitions[id] = definition
        }
        return definitions
    }

    private fun decodeOne(stream: CacheBuffer, rsInterface: CacheInterfaceDefinition) {
        rsInterface.type = stream.readUnsignedByte()
        rsInterface.atActionType = stream.readUnsignedByte()
        rsInterface.contentType = stream.readUnsignedShort()
        rsInterface.width = stream.readUnsignedShort()
        rsInterface.height = stream.readUnsignedShort()
        rsInterface.opacity = stream.readUnsignedByte().toByte()
        
        val hover = stream.readUnsignedByte()
        if (hover != 0) {
            rsInterface.hoverType = ((hover - 1) shl 8) + stream.readUnsignedByte()
        } else {
            rsInterface.hoverType = -1
        }
        
        val operators = stream.readUnsignedByte()
        if (operators > 0) {
            rsInterface.valueCompareType = IntArray(operators) { stream.readUnsignedByte() }
            rsInterface.requiredValues = IntArray(operators) { stream.readUnsignedShort() }
        }
        
        val scripts = stream.readUnsignedByte()
        if (scripts > 0) {
            rsInterface.scripts = Array(scripts) {
                val size = stream.readUnsignedShort()
                IntArray(size) { stream.readUnsignedShort() }
            }
        }
        
        if (rsInterface.type == 0) {
            rsInterface.scrollMax = stream.readUnsignedShort()
            rsInterface.isMouseoverTriggered = stream.readUnsignedByte() == 1
            val length = stream.readUnsignedShort()
            rsInterface.children = IntArray(length) { stream.readUnsignedShort() }
            rsInterface.childX = IntArray(length) { stream.readShort() }
            rsInterface.childY = IntArray(length) { stream.readShort() }
        }
        
        if (rsInterface.type == 1) {
            stream.readUnsignedShort()
            stream.readUnsignedByte()
        }
        
        if (rsInterface.type == 2) {
            rsInterface.inv = IntArray(rsInterface.width * rsInterface.height)
            rsInterface.invStackSizes = IntArray(rsInterface.width * rsInterface.height)
            rsInterface.invReplace = stream.readUnsignedByte() == 1
            rsInterface.isInventoryInterface = stream.readUnsignedByte() == 1
            rsInterface.usableItemInterface = stream.readUnsignedByte() == 1
            rsInterface.invStackable = stream.readUnsignedByte() == 1
            rsInterface.invSpritePadX = stream.readUnsignedByte()
            rsInterface.invSpritePadY = stream.readUnsignedByte()
            
            rsInterface.spritesX = IntArray(20)
            rsInterface.spritesY = IntArray(20)
            rsInterface.sprites = Array(20) { "" }
            
            for (j2 in 0 until 20) {
                val k3 = stream.readUnsignedByte()
                if (k3 == 1) {
                    rsInterface.spritesX!![j2] = stream.readShort()
                    rsInterface.spritesY!![j2] = stream.readShort()
                    rsInterface.sprites!![j2] = stream.readString()
                }
            }
            
            rsInterface.actions = Array(5) {
                val action = stream.readString()
                if (action.isEmpty()) null else action
            }
        }
        
        if (rsInterface.type == 3) {
            rsInterface.isFilled = stream.readUnsignedByte() == 1
        }
        
        if (rsInterface.type == 4 || rsInterface.type == 1) {
            rsInterface.centerText = stream.readUnsignedByte() == 1
            stream.readUnsignedByte() // font index
            rsInterface.textShadow = stream.readUnsignedByte() == 1
        }
        
        if (rsInterface.type == 4) {
            rsInterface.disabledMessage = stream.readString()
            rsInterface.enabledMessage = stream.readString()
        }
        
        if (rsInterface.type == 1 || rsInterface.type == 3 || rsInterface.type == 4) {
            rsInterface.textColor = stream.readInt()
        }
        
        if (rsInterface.type == 3 || rsInterface.type == 4) {
            rsInterface.anInt219 = stream.readInt()
            rsInterface.textHoverColor = stream.readInt()
            rsInterface.anInt239 = stream.readInt()
        }
        
        if (rsInterface.type == 5) {
            rsInterface.disabledSprite = stream.readString()
            rsInterface.enabledSprite = stream.readString()
        }
        
        if (rsInterface.type == 6) {
            var l = stream.readUnsignedByte()
            if (l != 0) {
                rsInterface.defaultMediaType = 1
                rsInterface.mediaID = ((l - 1) shl 8) + stream.readUnsignedByte()
            }
            l = stream.readUnsignedByte()
            if (l != 0) {
                rsInterface.anInt255 = 1
                rsInterface.anInt256 = ((l - 1) shl 8) + stream.readUnsignedByte()
            }
            l = stream.readUnsignedByte()
            if (l != 0) {
                rsInterface.anInt257 = ((l - 1) shl 8) + stream.readUnsignedByte()
            } else {
                rsInterface.anInt257 = -1
            }
            l = stream.readUnsignedByte()
            if (l != 0) {
                rsInterface.anInt258 = ((l - 1) shl 8) + stream.readUnsignedByte()
            } else {
                rsInterface.anInt258 = -1
            }
            rsInterface.modelZoom = stream.readUnsignedShort()
            rsInterface.modelRotation1 = stream.readUnsignedShort()
            rsInterface.modelRotation2 = stream.readUnsignedShort()
        }
        
        if (rsInterface.type == 7) {
            rsInterface.inv = IntArray(rsInterface.width * rsInterface.height)
            rsInterface.invStackSizes = IntArray(rsInterface.width * rsInterface.height)
            rsInterface.centerText = stream.readUnsignedByte() == 1
            stream.readUnsignedByte() // font index
            rsInterface.textShadow = stream.readUnsignedByte() == 1
            rsInterface.textColor = stream.readInt()
            rsInterface.invSpritePadX = stream.readShort()
            rsInterface.invSpritePadY = stream.readShort()
            rsInterface.isInventoryInterface = stream.readUnsignedByte() == 1
            rsInterface.actions = Array(5) {
                val action = stream.readString()
                if (action.isEmpty()) null else action
            }
        }
        
        if (rsInterface.atActionType == 2 || rsInterface.type == 2) {
            rsInterface.selectedActionName = stream.readString()
            rsInterface.spellName = stream.readString()
            rsInterface.spellUsableOn = stream.readUnsignedShort()
        }
        
        if (rsInterface.type == 8) {
            rsInterface.disabledMessage = stream.readString()
        }
        
        if (rsInterface.atActionType == 1 || rsInterface.atActionType == 4 || rsInterface.atActionType == 5 || rsInterface.atActionType == 6) {
            rsInterface.tooltip = stream.readString()
            if (rsInterface.tooltip.isEmpty()) {
                if (rsInterface.atActionType == 1) rsInterface.tooltip = "Ok"
                if (rsInterface.atActionType == 4) rsInterface.tooltip = "Select"
                if (rsInterface.atActionType == 5) rsInterface.tooltip = "Select"
                if (rsInterface.atActionType == 6) rsInterface.tooltip = "Continue"
            }
        }
    }
}

object CacheInterfaceDefinitions {
    @Volatile private var definitions: Map<Int, CacheInterfaceDefinition> = emptyMap()
    fun replace(next: Map<Int, CacheInterfaceDefinition>) { definitions = next.toMap() }
    @JvmStatic fun get(id: Int): CacheInterfaceDefinition? = definitions[id]
    @JvmStatic fun size(): Int = definitions.size
}
