package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition

data class NpcCacheOverride(
    val id: Int,
    val name: String? = null,
    val examine: String? = null,
    val size: Int? = null,
    val combatLevel: Int? = null,
    val standingAnimation: Int? = null,
    val walkingAnimation: Int? = null,
    val halfTurnAnimation: Int? = null,
    val clockwiseTurnAnimation: Int? = null,
    val anticlockwiseTurnAnimation: Int? = null,
    val actions: Map<Int, String?> = emptyMap(),
) {
    fun applyTo(definition: CacheNpcDefinition) {
        name?.let { definition.name = it }
        examine?.let { definition.examine = it }
        size?.let { definition.size = it }
        combatLevel?.let { definition.combatLevel = it }
        standingAnimation?.let { definition.standingAnimation = it }
        walkingAnimation?.let { definition.walkingAnimation = it }
        halfTurnAnimation?.let { definition.halfTurnAnimation = it }
        clockwiseTurnAnimation?.let { definition.clockwiseTurnAnimation = it }
        anticlockwiseTurnAnimation?.let { definition.anticlockwiseTurnAnimation = it }
        for ((slot, action) in actions) {
            definition.actions[slot - 1] = cleanText(action)
        }
    }
}

data class NpcServerPatch(
    val attackAnimation: Int? = null,
    val defenceAnimation: Int? = null,
    val deathAnimation: Int? = null,
    val respawnTicks: Int? = null,
    val attack: Int? = null,
    val strength: Int? = null,
    val defence: Int? = null,
    val hitpoints: Int? = null,
    val ranged: Int? = null,
    val magic: Int? = null,
    val headIcon: Int? = null,
    val transformTo: Int? = null,
) {
    fun overlay(child: NpcServerPatch): NpcServerPatch =
        NpcServerPatch(
            attackAnimation = child.attackAnimation ?: attackAnimation,
            defenceAnimation = child.defenceAnimation ?: defenceAnimation,
            deathAnimation = child.deathAnimation ?: deathAnimation,
            respawnTicks = child.respawnTicks ?: respawnTicks,
            attack = child.attack ?: attack,
            strength = child.strength ?: strength,
            defence = child.defence ?: defence,
            hitpoints = child.hitpoints ?: hitpoints,
            ranged = child.ranged ?: ranged,
            magic = child.magic ?: magic,
            headIcon = child.headIcon ?: headIcon,
            transformTo = child.transformTo ?: transformTo,
        )
}

data class NpcServerDefinition(
    val id: Int,
    val attackAnimation: Int? = null,
    val defenceAnimation: Int? = null,
    val deathAnimation: Int? = null,
    val respawnTicks: Int? = null,
    val attack: Int? = null,
    val strength: Int? = null,
    val defence: Int? = null,
    val hitpoints: Int? = null,
    val ranged: Int? = null,
    val magic: Int? = null,
    val headIcon: Int? = null,
    val transformTo: Int? = null,
) {
    fun asPatch(): NpcServerPatch =
        NpcServerPatch(
            attackAnimation = attackAnimation,
            defenceAnimation = defenceAnimation,
            deathAnimation = deathAnimation,
            respawnTicks = respawnTicks,
            attack = attack,
            strength = strength,
            defence = defence,
            hitpoints = hitpoints,
            ranged = ranged,
            magic = magic,
            headIcon = headIcon,
            transformTo = transformTo,
        )
}

@Deprecated("Use NpcServerDefinition. Runtime values are server-owned NPC values.")
typealias NpcRuntimeDefinition = NpcServerDefinition

data class NpcDisplayOverride(
    val headIcon: Int? = null,
    val transformTo: Int? = null,
)

/**
 * Server-side copy of decoded client-cache metadata.
 *
 * These values can affect server-owned uses such as dialogue labels or examine messages after
 * NpcData is built. They do not rewrite a connected player's cache: right-click names, right-click
 * option text, combat display, model size, and base stand/walk/turn rendering still come from the
 * client-visible NPC definition or display id unless future protocol/client work sends them.
 */
class NpcCacheOverrideBuilder internal constructor(private val id: Int) {
    var name: String? = null
    var examine: String? = null
    var size: Int? = null
    var combatLevel: Int? = null
    var standingAnimation: Int? = null
    var walkingAnimation: Int? = null
    var halfTurnAnimation: Int? = null
    var clockwiseTurnAnimation: Int? = null
    var anticlockwiseTurnAnimation: Int? = null
    private val actions = linkedMapOf<Int, String?>()

    /**
     * Records cache-action intent for validation/documentation. This does not rewrite a live
     * player's right-click menu unless the displayed client NPC definition already exposes it.
     */
    fun action(slot: Int, label: String?) {
        require(slot in 1..5) { "NPC cache action slot must be between 1 and 5. Got $slot." }
        actions[slot] = label
    }

    fun firstAction(label: String?) = action(1, label)
    fun secondAction(label: String?) = action(2, label)
    fun thirdAction(label: String?) = action(3, label)
    fun fourthAction(label: String?) = action(4, label)
    fun fifthAction(label: String?) = action(5, label)

    internal fun build(): NpcCacheOverride =
        NpcCacheOverride(
            id = id,
            name = cleanText(name),
            examine = cleanText(examine),
            size = positive(size),
            combatLevel = nonNegative(combatLevel),
            standingAnimation = positive(standingAnimation),
            walkingAnimation = positive(walkingAnimation),
            halfTurnAnimation = positive(halfTurnAnimation),
            clockwiseTurnAnimation = positive(clockwiseTurnAnimation),
            anticlockwiseTurnAnimation = positive(anticlockwiseTurnAnimation),
            actions = actions.toMap(),
        )
}

/**
 * Server-owned values that are currently consumed by NpcData or per-spawn NPC state.
 *
 * TODO:  useful future combat and metadata fields, but they should stay in
 * audits/import evidence until engine logic consumes them. Do not add live DSL properties for
 * maxHit, attackSpeed, aggressive, poisonous, venomous, poison/venom immunity, attack type/style,
 * weakness, slayer metadata, attributes/categories, combat bonuses, defence bonuses, or
 * server-side combat level until the combat/slayer systems read them.
 */
class NpcServerDefinitionBuilder internal constructor(private val id: Int? = null) {
    var attackAnimation: Int? = null
    var defenceAnimation: Int? = null
    var deathAnimation: Int? = null
    var respawnTicks: Int? = null
    var attack: Int? = null
    var strength: Int? = null
    var defence: Int? = null
    var hitpoints: Int? = null
    var ranged: Int? = null
    var magic: Int? = null
    var headIcon: Int? = null
    var transformTo: Int? = null

    fun stats(
        attack: Int? = null,
        strength: Int? = null,
        defence: Int? = null,
        hitpoints: Int? = null,
        ranged: Int? = null,
        magic: Int? = null,
    ) {
        this.attack = attack ?: this.attack
        this.strength = strength ?: this.strength
        this.defence = defence ?: this.defence
        this.hitpoints = hitpoints ?: this.hitpoints
        this.ranged = ranged ?: this.ranged
        this.magic = magic ?: this.magic
    }

    fun animations(attack: Int? = null, defence: Int? = null, death: Int? = null) {
        this.attackAnimation = attack ?: attackAnimation
        this.defenceAnimation = defence ?: defenceAnimation
        this.deathAnimation = death ?: deathAnimation
    }

    fun respawn(ticks: Int) {
        respawnTicks = ticks
    }

    fun display(headIcon: Int? = null, transformTo: Int? = null) {
        this.headIcon = headIcon ?: this.headIcon
        this.transformTo = transformTo ?: this.transformTo
    }

    internal fun buildDefinition(): NpcServerDefinition =
        NpcServerDefinition(
            id = requireNotNull(id) { "NpcServerDefinitionBuilder needs an id to build a definition" },
            attackAnimation = positive(attackAnimation),
            defenceAnimation = positive(defenceAnimation),
            deathAnimation = positive(deathAnimation),
            respawnTicks = positive(respawnTicks),
            attack = nonNegative(attack),
            strength = nonNegative(strength),
            defence = nonNegative(defence),
            hitpoints = positive(hitpoints),
            ranged = nonNegative(ranged),
            magic = nonNegative(magic),
            headIcon = nonNegative(headIcon),
            transformTo = nonNegative(transformTo),
        )

    internal fun buildPatch(): NpcServerPatch =
        NpcServerPatch(
            attackAnimation = positive(attackAnimation),
            defenceAnimation = positive(defenceAnimation),
            deathAnimation = positive(deathAnimation),
            respawnTicks = positive(respawnTicks),
            attack = nonNegative(attack),
            strength = nonNegative(strength),
            defence = nonNegative(defence),
            hitpoints = positive(hitpoints),
            ranged = nonNegative(ranged),
            magic = nonNegative(magic),
            headIcon = nonNegative(headIcon),
            transformTo = nonNegative(transformTo),
        )
}

@Deprecated("Use NpcServerDefinitionBuilder. Runtime values are server-owned NPC values.")
typealias NpcRuntimeDefinitionBuilder = NpcServerDefinitionBuilder

private fun cleanText(value: String?): String? =
    value
        ?.replace('_', ' ')
        ?.trim()
        ?.takeIf { it.isNotEmpty() && !it.equals("no name", true) && !it.equals("null", true) }

private fun positive(value: Int?): Int? = value?.takeIf { it > 0 }
private fun nonNegative(value: Int?): Int? = value?.takeIf { it >= 0 }
