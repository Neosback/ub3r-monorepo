package net.dodian.uber.game.shop

class ShopManager {
    init {
        reloadShops()
    }

    fun DiscountItem(ShopID: Int, ArrayID: Int) {
        ShopItemsN[ShopID][ArrayID] -= 1
        if (ShopItemsN[ShopID][ArrayID] <= 0 && ArrayID >= ShopItemsStandard[ShopID]) {
            ShopItemsN[ShopID][ArrayID] = 0
            ResetItem(ShopID, ArrayID)
        }
    }

    fun ResetItem(ShopID: Int, ArrayID: Int) {
        ShopItems[ShopID][ArrayID] = 0
        ShopItemsN[ShopID][ArrayID] = 0
        ShopItemsDelay[ShopID] = 0
    }

    @Suppress("UNUSED_PARAMETER")
    fun loadShops(FileName: String): Boolean = reloadShops()

    @Synchronized
    fun reloadShops(): Boolean {
        if (ShopItems.isEmpty()) {
            ShopItems = Array(MaxShops) { IntArray(MaxShopItems) }
            ShopItemsN = Array(MaxShops) { IntArray(MaxShopItems) }
            ShopItemsDelay = IntArray(MaxShops)
            ShopItemsSN = Array(MaxShops) { IntArray(MaxShopItems) }
            ShopItemsStandard = IntArray(MaxShops)
            ShopName = Array(MaxShops) { "" }
            ShopSModifier = IntArray(MaxShops)
            ShopBModifier = IntArray(MaxShops)
        }
        for (i in 0 until MaxShops) {
            for (j in 0 until MaxShopItems) {
                ResetItem(i, j)
                ShopItemsSN[i][j] = 0
            }
            ShopItemsStandard[i] = 0
            ShopSModifier[i] = 0
            ShopBModifier[i] = 0
            ShopName[i] = ""
        }
        var loadedShops = 0
        for (definition in ShopCatalog.all()) {
            require(definition.id in 0 until MaxShops) {
                "Shop ${definition.id} is outside the configured shop bounds (max=${MaxShops - 1})."
            }
            require(definition.stock.size <= MaxShopItems) {
                "Shop ${definition.id} exceeds MaxShopItems=$MaxShopItems with ${definition.stock.size} items."
            }
            ShopName[definition.id] = definition.name
            ShopSModifier[definition.id] = definition.sellModifier
            ShopBModifier[definition.id] = definition.buyModifier
            definition.stock.forEachIndexed { index, item ->
                ShopItems[definition.id][index] = item.itemId + 1
                ShopItemsN[definition.id][index] = item.amount
                ShopItemsSN[definition.id][index] = item.amount
                ShopItemsStandard[definition.id]++
            }
            loadedShops++
        }
        TotalShops = loadedShops
        return true
    }

    companion object {
        const val MaxShops: Int = 101
        const val MaxShopItems: Int = 40
        const val MaxShowDelay: Int = 100
        @JvmField var TotalShops: Int = 0

        @JvmField var ShopItems: Array<IntArray> = emptyArray()
        @JvmField var ShopItemsN: Array<IntArray> = emptyArray()
        @JvmField var ShopItemsDelay: IntArray = IntArray(0)
        @JvmField var ShopItemsSN: Array<IntArray> = emptyArray()
        @JvmField var ShopItemsStandard: IntArray = IntArray(0)
        @JvmField var ShopName: Array<String> = emptyArray()
        @JvmField var ShopSModifier: IntArray = IntArray(0)
        @JvmField var ShopBModifier: IntArray = IntArray(0)

        @JvmStatic
        fun resetAnItem(ShopID: Int, ArrayID: Int) {
            ShopItems[ShopID][ArrayID] = -1
            ShopItemsN[ShopID][ArrayID] = 0
            ShopItemsDelay[ShopID] = 0
        }

        @JvmStatic
        fun findDefaultItem(shopId: Int, id: Int): Boolean {
            if (ShopRulesService.isDefaultStockItem(shopId, id)) {
                return true
            }
            for (i in 0 until ShopItemsStandard[shopId]) {
                if (ShopItems[shopId][i] - 1 == id) {
                    return true
                }
            }
            return false
        }
    }
}
