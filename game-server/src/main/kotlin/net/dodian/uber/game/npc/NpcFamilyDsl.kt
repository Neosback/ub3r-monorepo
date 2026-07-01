package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

class NpcOptionsBuilder internal constructor() {
    private val bindings = ArrayList<NpcOptionBinding>()

    fun first(label: String = "first", handler: NpcClickHandler) = bind(1, label, handler)
    fun second(label: String = "second", handler: NpcClickHandler) = bind(2, label, handler)
    fun third(label: String = "third", handler: NpcClickHandler) = bind(3, label, handler)
    fun fourth(label: String = "fourth", handler: NpcClickHandler) = bind(4, label, handler)
    fun attack(label: String = "attack", handler: NpcClickHandler) = bind(5, label, handler)

    fun talkTo(label: String = "talk-to", handler: NpcClickHandler) = first(label, handler)
    fun trade(label: String = "trade", handler: NpcClickHandler) = second(label, handler)

    internal fun build(): List<NpcOptionBinding> = bindings.toList()

    private fun bind(option: Int, label: String, handler: NpcClickHandler) {
        require(bindings.none { it.option == option }) { "Duplicate NPC option $option" }
        bindings += NpcOptionBinding(option, label, handler)
    }
}

data class NpcOptionBinding(
    val option: Int,
    val label: String,
    val handler: NpcClickHandler,
)

class NpcSpawnsBuilder internal constructor(private val primaryId: Int) {
    private val values = ArrayList<NpcSpawnDef>()

    fun spawn(
        x: Int,
        y: Int,
        z: Int = 0,
        face: Int = NORTH,
        walkRadius: Int = 0,
        profile: NpcProfile? = null,
        hitpoints: Int = NPC_UNSET,
        attack: Int = NPC_UNSET,
        defence: Int = NPC_UNSET,
        strength: Int = NPC_UNSET,
        ranged: Int = NPC_UNSET,
        magic: Int = NPC_UNSET,
        respawnTicks: Int = NPC_UNSET,
        attackAnimation: Int = NPC_UNSET,
        deathAnimation: Int = NPC_UNSET,
        attackRange: Int = 6,
        alwaysActive: Boolean = false,
        block: NpcSpawnOverrideBuilder.() -> Unit = {},
    ) {
        spawnId(
            primaryId,
            x,
            y,
            z,
            face,
            walkRadius,
            profile,
            hitpoints,
            attack,
            defence,
            strength,
            ranged,
            magic,
            respawnTicks,
            attackAnimation,
            deathAnimation,
            attackRange,
            alwaysActive,
            block,
        )
    }

    fun spawnId(
        npcId: Int,
        x: Int,
        y: Int,
        z: Int = 0,
        face: Int = NORTH,
        walkRadius: Int = 0,
        profile: NpcProfile? = null,
        hitpoints: Int = NPC_UNSET,
        attack: Int = NPC_UNSET,
        defence: Int = NPC_UNSET,
        strength: Int = NPC_UNSET,
        ranged: Int = NPC_UNSET,
        magic: Int = NPC_UNSET,
        respawnTicks: Int = NPC_UNSET,
        attackAnimation: Int = NPC_UNSET,
        deathAnimation: Int = NPC_UNSET,
        attackRange: Int = 6,
        alwaysActive: Boolean = false,
        block: NpcSpawnOverrideBuilder.() -> Unit = {},
    ) {
        val base =
            NpcSpawnDef(
                npcId = npcId,
                x = x,
                y = y,
                z = z,
                face = face,
                walkRadius = walkRadius,
                attackRange = attackRange,
                alwaysActive = alwaysActive,
                respawnTicks = respawnTicks,
                attack = attack,
                defence = defence,
                strength = strength,
                hitpoints = hitpoints,
                ranged = ranged,
                magic = magic,
                attackAnimation = attackAnimation,
                deathAnimation = deathAnimation,
                profile = profile?.key,
            )
        values += NpcSpawnOverrideBuilder(base).apply(block).build()
    }

    internal fun build(): List<NpcSpawnDef> = values.toList()
}

class NpcFamilyBuilder internal constructor(
    private val name: String,
    private val primaryId: Int,
) {
    private val ids = linkedSetOf(primaryId)
    private val profiles = linkedSetOf<String>()
    private val cacheOverrides = ArrayList<NpcCacheOverride>()
    private val runtimeDefinitions = ArrayList<NpcRuntimeDefinition>()
    private var options = emptyList<NpcOptionBinding>()
    private var spawns = emptyList<NpcSpawnDef>()

    fun ids(vararg values: Int) {
        values.forEach { ids += it }
    }

    fun profiles(vararg values: String) {
        values.mapNotNullTo(profiles) { it.trim().takeIf(String::isNotEmpty) }
    }

    fun profile(value: String): NpcProfile =
        net.dodian.uber.game.npc.profile(value).also { profiles += it.key }

    fun options(block: NpcOptionsBuilder.() -> Unit) {
        options = NpcOptionsBuilder().apply(block).build()
    }

    fun cache(block: NpcCacheOverrideBuilder.() -> Unit) {
        cache(primaryId, block)
    }

    fun cache(id: Int, block: NpcCacheOverrideBuilder.() -> Unit) {
        ids += id
        cacheOverrides += NpcCacheOverrideBuilder(id).apply(block).build()
    }

    fun runtime(block: NpcRuntimeDefinitionBuilder.() -> Unit) {
        runtime(primaryId, block)
    }

    fun runtime(id: Int, block: NpcRuntimeDefinitionBuilder.() -> Unit) {
        ids += id
        runtimeDefinitions += NpcRuntimeDefinitionBuilder(id).apply(block).build()
    }

    fun spawns(block: NpcSpawnsBuilder.() -> Unit) {
        spawns = NpcSpawnsBuilder(primaryId).apply(block).build()
        ids += spawns.map { it.npcId }
    }

    internal fun build(): NpcFamily {
        val finalIds = ids.toIntArray()
        val optionLabels = options.associate { it.option to it.label }
        fun handler(option: Int): NpcClickHandler =
            options.firstOrNull { it.option == option }?.handler ?: NO_CLICK_HANDLER
        val content =
            NpcContentDefinition(
                name = name,
                npcIds = finalIds,
                profiles = profiles.toSet(),
                optionLabels = optionLabels,
                onFirstClick = handler(1),
                onSecondClick = handler(2),
                onThirdClick = handler(3),
                onFourthClick = handler(4),
                onAttack = handler(5),
                cacheOverrides = cacheOverrides.toList(),
                runtimeDefinitions = runtimeDefinitions.toList(),
            )
        return DefaultNpcFamily(name, primaryId, finalIds, content, spawns, cacheOverrides, runtimeDefinitions)
    }
}

private data class DefaultNpcFamily(
    override val familyName: String,
    override val primaryId: Int,
    override val ids: IntArray,
    override val definition: NpcContentDefinition,
    override val spawns: List<NpcSpawnDef>,
    override val cacheOverrides: List<NpcCacheOverride>,
    override val runtimeDefinitions: List<NpcRuntimeDefinition>,
) : NpcFamily

fun npcFamily(name: String, primaryId: Int, block: NpcFamilyBuilder.() -> Unit): NpcFamily =
    NpcFamilyBuilder(name, primaryId).apply(block).build()

fun noNpcClick(@Suppress("UNUSED_PARAMETER") client: Client, @Suppress("UNUSED_PARAMETER") npc: Npc): Boolean = false
