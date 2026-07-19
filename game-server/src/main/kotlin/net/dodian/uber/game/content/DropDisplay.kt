package net.dodian.uber.game.content

import net.dodian.uber.game.Server
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.SendItemOnInterfaceSlot
import net.dodian.uber.game.netty.listener.out.SendScrollbar
import net.dodian.uber.game.netty.listener.out.SendString
import net.dodian.uber.game.netty.listener.out.SendTooltip
import net.dodian.uber.game.netty.listener.out.ShowInterface
import net.dodian.uber.game.model.entity.npc.NpcData
import java.util.ArrayList
import java.util.Locale

object DropDisplay {

    enum class DropType {
        ITEM,
        NPC
    }

    private val DEFAULT = arrayOf(
        "King black dragon",
        "Skotizo",
        "Zulrah",
        "Callisto",
        "Commander Zilyana",
        "Venenatis",
        "Kraken",
        "Chaos Fanatic",
        "Scorpia",
        "Corporeal Beast",
        "Lizardman Shaman",
        "Cerberus",
        "Vorkath",
        "Skeletal Wyvern"
    )

    @JvmStatic
    fun open(client: Client) {
        val randomName = DEFAULT.random()
        search(client, randomName, DropType.NPC)
        client.openInterface(54500)
    }

    @JvmStatic
    fun search(client: Client, contextInput: String, type: DropType): Boolean {
        val context = contextInput.trim().lowercase(Locale.US)
        val npcNames = ArrayList<String>()
        val npcIds = ArrayList<Int>()

        // Search in all NPC definitions
        for (npcId in 0 until 16000) {
            val npcData = Server.npcManager.getData(npcId) ?: continue
            val name = npcData.name
            if (name.isEmpty() || npcData.drops.isEmpty()) continue

            if (type == DropType.NPC) {
                if (name.lowercase(Locale.US).contains(context)) {
                    val formattedName = name.replace("_", " ")
                    if (!npcNames.contains(formattedName)) {
                        npcNames.add(formattedName)
                        npcIds.add(npcId)
                    }
                }
            } else if (type == DropType.ITEM) {
                for (drop in npcData.drops) {
                    val itemName = client.getItemName(drop.id) ?: continue
                    if (itemName.lowercase(Locale.US).contains(context)) {
                        val formattedName = name.replace("_", " ")
                        if (!npcNames.contains(formattedName)) {
                            npcNames.add(formattedName)
                            npcIds.add(npcId)
                        }
                    }
                }
            }
        }

        if (npcIds.isEmpty()) {
            client.dropDisplayKey = emptyList()
            client.sendMessage("No search was found for your entry!")
            return false
        }

        val size = if (npcNames.size < 10) 10 else npcNames.size
        var stringId = 54516
        for (index in 0 until size) {
            if (stringId >= 54551) break
            val name = if (index >= npcNames.size) "" else npcNames[index]
            client.send(SendTooltip(if (name.isEmpty()) "" else "View drop table of <col=ff9933>$name", stringId))
            client.send(SendString(name, stringId + 1))
            stringId += 2
        }

        client.dropDisplayKey = npcIds
        client.send(SendScrollbar(54515, 280))

        // Display the drops of the first matched NPC
        val firstNpcData = Server.npcManager.getData(npcIds[0])
        display(client, firstNpcData)
        return true
    }

    @JvmStatic
    fun display(client: Client, npcData: NpcData?) {
        if (npcData == null) {
            client.send(SendScrollbar(54550, 350))
            client.send(SendString("Drops: 0", 54514))
            client.send(SendString("", 54513))
            // Clear items
            var stringId = 54552
            for (index in 0 until 9) {
                client.send(SendItemOnInterfaceSlot(54551, -1, 0, index))
                client.send(SendString("", stringId + 1))
                client.send(SendString("", stringId + 2))
                client.send(SendString("", stringId + 3))
                client.send(SendString("", stringId + 4))
                stringId += 5
            }
            return
        }

        val drops = npcData.drops
        val size = if (drops.size < 9) 9 else drops.size
        client.send(SendScrollbar(54550, size * 32))
        client.send(SendString("Drops: ${drops.size}", 54514))
        client.send(SendString(npcData.name.replace("_", " "), 54513))

        var stringId = 54552
        for (index in 0 until size) {
            val valid = index < drops.size
            if (valid) {
                val drop = drops[index]
                client.send(SendItemOnInterfaceSlot(54551, drop.id, drop.maxAmount, index))
                val itemName = client.getItemName(drop.id) ?: "Item ${drop.id}"
                client.send(SendString(itemName, stringId + 1))
                client.send(SendString(formatDigits(drop.minAmount), stringId + 2))
                client.send(SendString(formatDigits(drop.maxAmount), stringId + 3))
                client.send(SendString(formatRarity(drop.chance), stringId + 4))
            } else {
                client.send(SendItemOnInterfaceSlot(54551, -1, 0, index))
                client.send(SendString("", stringId + 1))
                client.send(SendString("", stringId + 2))
                client.send(SendString("", stringId + 3))
                client.send(SendString("", stringId + 4))
            }
            stringId += 5
        }
    }

    private fun formatDigits(amount: Int): String {
        return java.text.NumberFormat.getIntegerInstance().format(amount)
    }

    private fun formatRarity(percent: Double): String {
        val name = when {
            percent >= 99.0 -> "Always"
            percent >= 10.0 -> "Common"
            percent >= 2.0 -> "Uncommon"
            percent >= 0.4 -> "Rare"
            else -> "Very Rare"
        }
        val formattedPercent = String.format(Locale.US, "%.1f%%", percent)
        return "$name ($formattedPercent)"
    }
}
