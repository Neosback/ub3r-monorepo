package net.dodian.uber.game.item

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader
import java.util.HashSet
import java.util.LinkedHashMap
import net.dodian.uber.game.engine.systems.world.item.GlobalGroundItemSpawns
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.item.Item
import net.dodian.uber.game.netty.listener.out.SendMessage
import org.slf4j.LoggerFactory

fun parseItemsJsonDir(dirPath: String): List<ItemDefJson> {
    val dir = File(dirPath)
    if (!dir.exists() || !dir.isDirectory) return emptyList()
    val gson = Gson()
    return dir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
        ?.map { file ->
            try {
                JsonReader(FileReader(file)).use { reader ->
                    gson.fromJson<ItemDefJson>(reader, ItemDefJson::class.java)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Failed to parse items-json file ${file.name}: ${e.message}", e)
            }
        } ?: emptyList()
}

fun parseItemDefsFile(filePath: String): List<ItemDefBase> {
    val file = File(filePath)
    if (!file.exists()) return emptyList()
    val gson = Gson()
    return try {
        JsonReader(FileReader(file)).use { reader ->
            val type = object : TypeToken<List<ItemDefBase>>() {}.type
            gson.fromJson<List<ItemDefBase>>(reader, type)
        }
    } catch (e: Exception) {
        throw IllegalStateException("Failed to parse item definitions: ${e.message}", e)
    }
}

fun parseTarnishAppearanceTypes(filePath: String): Map<Int, TarnishEquipmentAppearanceType> {
    val file = File(filePath)
    if (!file.exists()) return emptyMap()
    return file.useLines { lines ->
        lines.filter { it.isNotBlank() && !it.startsWith("#") }
            .associate { line ->
                val parts = line.split(',', limit = 2)
                require(parts.size == 2) { "Invalid Tarnish appearance type row: $line" }
                parts[0].trim().toInt() to TarnishEquipmentAppearanceType.valueOf(parts[1].trim())
            }
    }
}

open class ItemManager @JvmOverloads constructor(
    private val definitionLoader: (() -> Map<Int, Item>)? = null,
    private val globalSpawnBootstrap: (() -> Unit)? = null,
) {
    @JvmField
    var items: Array<Item?> = arrayOfNulls(35000)

    private val logger = LoggerFactory.getLogger(ItemManager::class.java)
    private val defaultStandAnim = 808
    private val defaultWalkAnim = 819
    private val defaultRunAnim = 824
    private val defaultAttackAnim = 806

    init {
        loadGlobalItems()
        loadItems()
    }

    open fun loadGlobalItems() {
        val bootstrap = globalSpawnBootstrap
        if (bootstrap != null) {
            bootstrap()
            return
        }
        GlobalGroundItemSpawns.spawnAll()
    }

    open fun loadItems() {
        val loaded = definitionLoader?.invoke() ?: loadItemsFromJson()
        
        // Integrity check: assert noted/unnoted item references point to existing items
        for (item in loaded.values) {
            if (item.isNoted() && item.linkedItemId > 0 && !loaded.containsKey(item.linkedItemId)) {
                throw IllegalStateException("Item definition validation failed: Item ID ${item.id} (${item.getName()}) is noted but its linkedItemId ${item.linkedItemId} does not exist.")
            }
            if (!item.isNoted() && !item.isPlaceholder() && item.linkedNotedId > 0 && !loaded.containsKey(item.linkedNotedId)) {
                throw IllegalStateException("Item definition validation failed: Item ID ${item.id} (${item.getName()}) is noteable but its linkedNotedId ${item.linkedNotedId} does not exist.")
            }
        }

        val maxId = loaded.keys.maxOrNull() ?: 0
        val size = maxOf(35000, maxId + 1)
        val newItems = arrayOfNulls<Item>(size)
        for ((id, item) in loaded) {
            newItems[id] = item
        }
        items = newItems
        logger.info("item_definitions_ready count={}", loaded.size)
    }

    private fun loadItemsFromJson(): Map<Int, Item> {
        val startTime = System.currentTimeMillis()
        val loaded = LinkedHashMap<Int, Item>()

        val baseDefs = parseItemDefsFile("data/def/item/item_definitions.json")
        val tarnishAppearanceTypes = parseTarnishAppearanceTypes("data/def/item/tarnish_equipment_appearance.csv")
        val baseMap = LinkedHashMap<Int, ItemDefBase>()
        for (base in baseDefs) {
            baseMap[base.id] = base
        }
        logger.debug("Loaded {} base item definitions", baseMap.size)

        val jsonDefs = parseItemsJsonDir("data/def/items-json")
        logger.debug("Loaded {} items-json definitions", jsonDefs.size)

        val processedIds = LinkedHashSet<Int>()
        for (json in jsonDefs) {
            val base = baseMap[json.id]
            if (base == null) {
                val item = Item(
                    id = json.id,
                    name = json.name,
                    slot = 0,
                    standAnim = defaultStandAnim,
                    walkAnim = defaultWalkAnim,
                    runAnim = defaultRunAnim,
                    attackAnim = defaultAttackAnim,
                    shopSellValue = json.cost,
                    shopBuyValue = json.cost,
                    bonuses = json.equipment?.toBonusArray() ?: IntArray(14),
                    stackable = json.stackable || json.noted,
                    noted = json.noted,
                    placeholder = json.placeholder,
                    noteable = json.noteable,
                    tradeable = json.tradeable,
                    twoHanded = json.equipment?.slot == "2h",
                    full = false,
                    mask = false,
                    premium = json.members,
                    examine = json.examine ?: "It's a ${json.name}.",
                    alchemy = json.highAlch,
                    weight = json.weight,
                    lowAlch = json.lowAlch,
                    linkedItemId = json.linkedIdItem ?: 0,
                    linkedNotedId = json.linkedIdNoted ?: 0,
                    attackSpeed = json.weapon?.attackSpeed ?: 4,
                    weaponType = json.weapon?.weaponType ?: "",
                    stances = json.weapon?.stances ?: emptyArray(),
                )
                loaded[json.id] = item
            } else {
                loaded[json.id] = Item.fromDefs(
                    base,
                    json,
                    tarnishAppearanceTypes[json.id] ?: TarnishEquipmentAppearanceType.DEFAULT,
                )
            }
            processedIds.add(json.id)
        }

        for ((id, base) in baseMap) {
            if (id !in processedIds) {
                loaded[id] = Item.fromDefs(
                    base,
                    null,
                    tarnishAppearanceTypes[id] ?: TarnishEquipmentAppearanceType.DEFAULT,
                )
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        logger.debug("Built {} item definitions in {}ms", loaded.size, elapsed)
        return loaded
    }

    private fun getItem(id: Int): Item? {
        if (id < 0 || id >= items.size) return null
        return items[id]
    }

    /** True only for an item that exists in the server's loaded definition set. */
    fun hasDefinition(id: Int): Boolean = getItem(id) != null

    fun isNote(id: Int): Boolean {
        val item = getItem(id)
        return item != null && id >= 0 && item.isNoted()
    }

    fun getLinkedItemId(id: Int): Int {
        val item = getItem(id) ?: return 0
        return if (item.isNoted()) item.linkedItemId else 0
    }

    fun getLinkedNotedId(id: Int): Int {
        val item = getItem(id) ?: return 0
        return if (!item.isNoted() && !item.isPlaceholder()) item.linkedNotedId else 0
    }

    fun normalizeForBank(id: Int): Int = getLinkedItemId(id).takeIf { it > 0 } ?: id

    fun isStackable(id: Int): Boolean {
        val item = getItem(id)
        return item != null && id >= 0 && item.getStackable()
    }

    fun isTwoHanded(id: Int): Boolean {
        val item = getItem(id)
        return id >= 0 && item != null && item.getTwoHanded()
    }

    fun getSlot(id: Int): Int {
        if (id < 0) return 3
        val item = getItem(id) ?: return 3
        return item.slot
    }

    fun getStandAnim(id: Int): Int {
        if (id < 1) return defaultStandAnim
        val item = getItem(id) ?: return defaultStandAnim
        return item.getStandAnim()
    }

    fun getWalkAnim(id: Int): Int {
        if (id < 1) return defaultWalkAnim
        val item = getItem(id) ?: return defaultWalkAnim
        return item.getWalkAnim()
    }

    fun getRunAnim(id: Int): Int {
        if (id < 1) return defaultRunAnim
        val item = getItem(id) ?: return defaultRunAnim
        return item.getRunAnim()
    }

    fun getAttackAnim(id: Int): Int {
        if (id < 1) return defaultAttackAnim
        val item = getItem(id) ?: return defaultAttackAnim
        return item.getAttackAnim()
    }

    fun isPremium(id: Int): Boolean {
        if (id < 0) return false
        val item = getItem(id) ?: return false
        return item.getPremium()
    }

    fun isTradable(id: Int): Boolean {
        if (id < 0 || id == 4084) return false
        val item = getItem(id) ?: return false
        return item.getTradeable()
    }

    fun getBonus(id: Int, bonus: Int): Int {
        if (id < 0 || bonus < 0) return 0
        val item = getItem(id) ?: return 0
        val bonuses = item.getBonuses()
        return if (bonus < bonuses.size) bonuses[bonus] else 0
    }

    fun isFullBody(id: Int): Boolean {
        if (id < 0) return false
        val item = getItem(id)
        return item != null && item.slot == 4 && item.full
    }

    fun isFullHelm(id: Int): Boolean {
        if (id < 0) return false
        val item = getItem(id)
        return item != null && item.slot == 0 && item.full
    }

    fun isMask(id: Int): Boolean {
        if (id < 0) return false
        val item = getItem(id)
        return item != null && item.slot == 0 && item.mask
    }

    fun getTarnishAppearanceType(id: Int): TarnishEquipmentAppearanceType =
        getItem(id)?.tarnishAppearanceType ?: TarnishEquipmentAppearanceType.DEFAULT

    fun getShopSellValue(id: Int): Int = getItem(id)?.getShopSellValue() ?: 1

    fun getShopBuyValue(id: Int): Int = getItem(id)?.getShopBuyValue() ?: 0

    fun getAlchemy(id: Int): Int = getItem(id)?.getAlchemy() ?: 0

    fun getName(id: Int): String =
        getItem(id)?.getName()?.replace("_", " ")
            ?: "Database Error. Please contact admins with this error code: ITEM_NAME_$id"

    fun getExamine(id: Int): String = getItem(id)?.getDescription()?.replace("_", " ") ?: ""

    fun getWeight(id: Int): Double = getItem(id)?.weight ?: 0.0

    fun getAttackSpeed(id: Int): Int = getItem(id)?.attackSpeed ?: 4

    fun getWeaponType(id: Int): String = getItem(id)?.weaponType ?: ""

    fun getStances(id: Int): Array<ItemWeaponStance> = getItem(id)?.stances ?: emptyArray()

    fun getItemName(c: Client, name: String) {
        var send = false
        val normalizedName = name.replace("_", " ")
        for (item in items) {
            if (item != null && normalizedName.equals(item.getName().replace("_", " "), ignoreCase = true) &&
                item.getDescription() != "null"
            ) {
                var prefix = ""
                if (isNote(item.id)) {
                    prefix = " (NOTED)"
                }
                c.send(
                    SendMessage(
                        normalizedName.replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase() else char.toString()
                        } + prefix +
                            ": Sell Price: ${item.getShopBuyValue()}. Alchemy Price: ${item.getAlchemy()}",
                    ),
                )
                send = true
            }
        }
        if (!send) {
            c.sendMessage("Could not find the item $normalizedName in the database!")
        }
    }

    open fun reloadItems() {
        loadItems()
    }
}
