package net.dodian.uber.game.content.npcs

@Deprecated("Use net.dodian.uber.game.npc.simpleNpcDefinition")
fun simpleNpcDefinition(
    name: String,
    entries: List<NpcSpawnDef>,
): NpcContentDefinition =
    net.dodian.uber.game.npc.simpleNpcDefinition(name, entries)

@Deprecated("Use net.dodian.uber.game.npc.legacyNpcDefinition")
fun legacyNpcDefinition(
    name: String,
    entries: List<NpcSpawnDef>,
    onFirstClick: NpcClickHandler = NO_CLICK_HANDLER,
    onSecondClick: NpcClickHandler = NO_CLICK_HANDLER,
    onThirdClick: NpcClickHandler = NO_CLICK_HANDLER,
    onFourthClick: NpcClickHandler = NO_CLICK_HANDLER,
    onAttack: NpcClickHandler = NO_CLICK_HANDLER,
): NpcContentDefinition {
    return net.dodian.uber.game.npc.legacyNpcDefinition(
        name = name,
        entries = entries,
        onFirstClick = onFirstClick,
        onSecondClick = onSecondClick,
        onThirdClick = onThirdClick,
        onFourthClick = onFourthClick,
        onAttack = onAttack,
    )
}
