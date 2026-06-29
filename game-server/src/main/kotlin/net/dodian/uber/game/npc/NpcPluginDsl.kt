package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

typealias NpcClickHandlerWithState = NpcPluginContext.(Client, Npc) -> Boolean

class NpcOptionsBuilder internal constructor(
    private val stateNamespace: String,
) {
    private val bindings = ArrayList<NpcOptionBinding>()

    fun first(label: String, handler: (Client, Npc) -> Boolean) = bind(NpcOptionSlot.FIRST, label, handler)

    fun second(label: String, handler: (Client, Npc) -> Boolean) = bind(NpcOptionSlot.SECOND, label, handler)

    fun third(label: String, handler: (Client, Npc) -> Boolean) = bind(NpcOptionSlot.THIRD, label, handler)

    fun fourth(label: String, handler: (Client, Npc) -> Boolean) = bind(NpcOptionSlot.FOURTH, label, handler)

    fun attack(label: String, handler: (Client, Npc) -> Boolean) = bind(NpcOptionSlot.ATTACK, label, handler)

    fun first(
        label: String,
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = bind(NpcOptionSlot.FIRST, label, buildSequentialHandler(init))

    fun second(
        label: String,
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = bind(NpcOptionSlot.SECOND, label, buildSequentialHandler(init))

    fun third(
        label: String,
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = bind(NpcOptionSlot.THIRD, label, buildSequentialHandler(init))

    fun fourth(
        label: String,
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = bind(NpcOptionSlot.FOURTH, label, buildSequentialHandler(init))

    fun attack(
        label: String,
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = bind(NpcOptionSlot.ATTACK, label, buildSequentialHandler(init))

    fun talkTo(
        label: String = "talk-to",
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = first(label, init)

    fun trade(
        label: String = "trade",
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = second(label, init)

    fun teleportOption(
        label: String = "teleport",
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = third(label, init)

    fun option4(
        label: String = "option-4",
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = fourth(label, init)

    fun attackOption(
        label: String = "attack",
        init: NpcSequentialOptionBuilder.() -> Unit,
    ) = attack(label, init)

    fun firstState(label: String, handler: NpcClickHandlerWithState) = bindWithState(NpcOptionSlot.FIRST, label, handler)

    fun secondState(label: String, handler: NpcClickHandlerWithState) = bindWithState(NpcOptionSlot.SECOND, label, handler)

    fun thirdState(label: String, handler: NpcClickHandlerWithState) = bindWithState(NpcOptionSlot.THIRD, label, handler)

    fun fourthState(label: String, handler: NpcClickHandlerWithState) = bindWithState(NpcOptionSlot.FOURTH, label, handler)

    fun attackState(label: String, handler: NpcClickHandlerWithState) = bindWithState(NpcOptionSlot.ATTACK, label, handler)

    internal fun build(): List<NpcOptionBinding> = bindings.toList()

    private fun bindWithState(slot: NpcOptionSlot, label: String, handler: NpcClickHandlerWithState) {
        require(bindings.none { it.slot == slot }) { "Duplicate option binding for slot=$slot label='$label'." }
        bindings += NpcOptionBinding(slot, label) { client, npc ->
            val context = NpcPluginContext(client = client, stateNamespace = stateNamespace)
            handler(context, client, npc)
        }
    }

    private fun bind(slot: NpcOptionSlot, label: String, handler: (Client, Npc) -> Boolean) {
        require(bindings.none { it.slot == slot }) { "Duplicate option binding for slot=$slot label='$label'." }
        bindings += NpcOptionBinding(slot, label, handler)
    }
}

class NpcPluginBuilder internal constructor(
    private val name: String,
) {
    private val npcIds = LinkedHashSet<Int>()
    private val profiles = LinkedHashSet<String>()
    private var optionBindings: List<NpcOptionBinding> = emptyList()
    private var stateNamespace: String = name
    val definitionOverrides = ArrayList<NpcDefinitionOverrideJson>()

    fun ids(vararg ids: Int) {
        ids.forEach { npcIds += it }
    }

    fun profiles(vararg values: String) {
        values
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { profiles += it }
    }

    fun options(block: NpcOptionsBuilder.() -> Unit) {
        val builder = NpcOptionsBuilder(stateNamespace = stateNamespace)
        builder.block()
        optionBindings = builder.build()
    }

    fun npc(id: Int, block: NpcDefinitionOverrideBuilder.() -> Unit) {
        definitionOverrides += NpcDefinitionOverrideBuilder(id).apply(block).build()
    }

    fun state(namespace: String) {
        stateNamespace = namespace
    }

    fun build(): NpcPluginDefinition {
        return NpcPluginDefinition(
            name = name,
            npcIds = npcIds.toIntArray(),
            profiles = profiles.toSet(),
            optionBindings = optionBindings,
            definitionOverrides = definitionOverrides,
            stateNamespace = stateNamespace,
        )
    }
}

fun npcPlugin(name: String, init: NpcPluginBuilder.() -> Unit): NpcPluginDefinition =
    NpcPluginBuilder(name).apply(init).build()

class NpcDefinitionOverrideBuilder(val id: Int) {
    var name: String? = null
    var examine: String? = null
    var size: Int? = null
    var combatLevel: Int? = null
    var standingAnimation: Int? = null
    var walkingAnimation: Int? = null
    var attackAnimation: Int? = null
    var deathAnimation: Int? = null
    var respawnTicks: Int? = null
    var attack: Int? = null
    var strength: Int? = null
    var defence: Int? = null
    var hitpoints: Int? = null
    var ranged: Int? = null
    var magic: Int? = null
    var actions: List<String?>? = null

    fun build() = NpcDefinitionOverrideJson(
        id = id,
        name = name,
        examine = examine,
        size = size,
        combatLevel = combatLevel,
        standingAnimation = standingAnimation,
        walkingAnimation = walkingAnimation,
        attackAnimation = attackAnimation,
        deathAnimation = deathAnimation,
        respawnTicks = respawnTicks,
        attack = attack,
        strength = strength,
        defence = defence,
        hitpoints = hitpoints,
        ranged = ranged,
        magic = magic,
        actions = actions
    )
}
