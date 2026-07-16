package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.skill.runtime.action.SkillStateCoordinator

class SkillPluginBuilder internal constructor(
    private val name: String,
    private val skill: Skill,
) {
    private val objectBindings = ArrayList<SkillObjectClickBinding>()
    private val npcBindings = ArrayList<SkillNpcClickBinding>()
    private val itemOnItemBindings = ArrayList<SkillItemOnItemBinding>()
    private val itemBindings = ArrayList<SkillItemClickBinding>()
    private val itemOnObjectBindings = ArrayList<SkillItemOnObjectBinding>()
    private val magicOnObjectBindings = ArrayList<SkillMagicOnObjectBinding>()
    private val buttonBindings = ArrayList<SkillButtonBinding>()
    private var lifecycle = SkillPluginLifecycleHooks()

    fun objectClick(
        preset: PolicyPreset,
        option: Int = 1,
        vararg objectIds: Int,
        handler: (SkillObjectInteraction) -> Boolean,
    ) {
        require(option in 1..5) { "Object option must be between 1 and 5." }
        require(objectIds.isNotEmpty()) { "Object click binding requires at least one object id." }
        objectBindings += SkillObjectClickBinding(preset, option, objectIds.toSet().toIntArray(), handler)
    }

    fun npcClick(
        preset: PolicyPreset,
        option: Int,
        vararg npcIds: Int,
        handler: (SkillNpcInteraction) -> Boolean,
    ) {
        require(option in 1..4) { "NPC option must be between 1 and 4." }
        require(npcIds.isNotEmpty()) { "Npc click binding requires at least one npc id." }
        npcBindings += SkillNpcClickBinding(preset, option, npcIds.toSet().toIntArray(), handler)
    }

    fun itemOnItem(
        preset: PolicyPreset,
        leftItemId: Int,
        rightItemId: Int,
        handler: (SkillItemOnItemInteraction) -> Boolean,
    ) {
        require(leftItemId >= 0 && rightItemId >= 0) { "Item ids must be non-negative." }
        itemOnItemBindings += SkillItemOnItemBinding(preset, leftItemId, rightItemId, handler)
    }

    fun itemClick(
        preset: PolicyPreset,
        option: Int,
        vararg itemIds: Int,
        handler: (SkillItemInteraction) -> Boolean,
    ) {
        require(option in 1..3) { "Item option must be between 1 and 3." }
        require(itemIds.isNotEmpty()) { "Item click binding requires at least one item id." }
        itemBindings += SkillItemClickBinding(preset, option, itemIds.toSet().toIntArray(), handler)
    }

    fun itemOnObject(
        preset: PolicyPreset,
        vararg objectIds: Int,
        itemIds: IntArray = intArrayOf(-1),
        handler: (SkillItemOnObjectInteraction) -> Boolean,
    ) {
        require(objectIds.isNotEmpty()) { "Item-on-object binding requires at least one object id." }
        require(itemIds.isNotEmpty()) { "Item-on-object binding requires at least one item id or wildcard (-1)." }
        itemOnObjectBindings += SkillItemOnObjectBinding(
            preset = preset,
            objectIds = objectIds.toSet().toIntArray(),
            itemIds = itemIds.toSet().toIntArray(),
            handler = handler,
        )
    }

    fun magicOnObject(
        preset: PolicyPreset,
        vararg objectIds: Int,
        spellIds: IntArray = intArrayOf(-1),
        handler: (SkillMagicOnObjectInteraction) -> Boolean,
    ) {
        require(objectIds.isNotEmpty()) { "Magic-on-object binding requires at least one object id." }
        require(spellIds.isNotEmpty()) { "Magic-on-object binding requires at least one spell id or wildcard (-1)." }
        magicOnObjectBindings += SkillMagicOnObjectBinding(
            preset = preset,
            objectIds = objectIds.toSet().toIntArray(),
            spellIds = spellIds.toSet().toIntArray(),
            handler = handler,
        )
    }

    fun button(
        preset: PolicyPreset,
        requiredInterfaceId: Int = -1,
        opIndex: Int? = null,
        vararg rawButtonIds: Int,
        handler: (SkillButtonInteraction) -> Boolean,
    ) {
        require(rawButtonIds.isNotEmpty()) { "Button binding requires at least one raw button id." }
        require(requiredInterfaceId >= -1) { "Button requiredInterfaceId must be -1 or a non-negative interface id." }
        buttonBindings += SkillButtonBinding(preset, rawButtonIds.toSet().toIntArray(), requiredInterfaceId, opIndex, handler)
    }

    fun lifecycle(block: SkillPluginLifecycleBuilder.() -> Unit) {
        lifecycle = SkillPluginLifecycleBuilder().apply(block).build()
    }

    fun startSession(sessionKey: String) {
        composeLifecycle(
            onStart = { it.actions.beginSession(sessionKey) },
        )
    }

    fun requireSession(sessionKey: String) {
        composeLifecycle(
            onAttempt = { client ->
                val existing = client.actions.activeSessionKey()
                if (existing != null && existing != sessionKey) {
                    client.ui.message("You are already doing another skill action.")
                }
            },
        )
    }

    fun endSession(sessionKey: String) {
        composeLifecycle(
            onStop = { it.actions.endSession(sessionKey) },
        )
    }

    private fun composeLifecycle(
        onAttempt: ((SkillPlayer) -> Unit)? = null,
        onStart: ((SkillPlayer) -> Unit)? = null,
        onCycle: ((SkillPlayer) -> Unit)? = null,
        onStop: ((SkillPlayer) -> Unit)? = null,
    ) {
        lifecycle =
            SkillPluginLifecycleHooks(
                onAttempt = compose(lifecycle.onAttempt, onAttempt),
                onStart = compose(lifecycle.onStart, onStart),
                onCycle = compose(lifecycle.onCycle, onCycle),
                onStop = compose(lifecycle.onStop, onStop),
            )
    }

    private fun compose(
        first: ((SkillPlayer) -> Unit)?,
        second: ((SkillPlayer) -> Unit)?,
    ): ((SkillPlayer) -> Unit)? {
        if (first == null) return second
        if (second == null) return first
        return { client ->
            first(client)
            second(client)
        }
    }

    internal fun build(): SkillPluginDefinition {
        return SkillPluginDefinition(
            name = name,
            skill = skill,
            objectBindings = objectBindings.toList(),
            npcBindings = npcBindings.toList(),
            itemOnItemBindings = itemOnItemBindings.toList(),
            itemBindings = itemBindings.toList(),
            itemOnObjectBindings = itemOnObjectBindings.toList(),
            magicOnObjectBindings = magicOnObjectBindings.toList(),
            buttonBindings = buttonBindings.toList(),
            lifecycle = lifecycle,
        )
    }
}

class SkillPluginLifecycleBuilder internal constructor() {
    private var onAttempt: ((SkillPlayer) -> Unit)? = null
    private var onStart: ((SkillPlayer) -> Unit)? = null
    private var onCycle: ((SkillPlayer) -> Unit)? = null
    private var onStop: ((SkillPlayer) -> Unit)? = null

    fun onAttempt(handler: (SkillPlayer) -> Unit) {
        onAttempt = handler
    }

    fun onStart(handler: (SkillPlayer) -> Unit) {
        onStart = handler
    }

    fun onCycle(handler: (SkillPlayer) -> Unit) {
        onCycle = handler
    }

    fun onStop(handler: (SkillPlayer) -> Unit) {
        onStop = handler
    }

    internal fun build(): SkillPluginLifecycleHooks {
        return SkillPluginLifecycleHooks(onAttempt, onStart, onCycle, onStop)
    }
}

fun skillPlugin(
    name: String,
    skill: Skill,
    block: SkillPluginBuilder.() -> Unit,
): SkillPluginDefinition {
    return SkillPluginBuilder(name, skill).apply(block).build()
}
