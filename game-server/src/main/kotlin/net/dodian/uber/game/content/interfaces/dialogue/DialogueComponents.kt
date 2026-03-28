package net.dodian.uber.game.content.interfaces.dialogue

import net.dodian.uber.game.content.platform.InterfaceMappingRegistry

object DialogueComponents {
    const val INTERFACE_ID = 0

    val optionOne: IntArray
        get() = InterfaceMappingRegistry.dialogueData().optionOne
    val optionTwo: IntArray
        get() = InterfaceMappingRegistry.dialogueData().optionTwo
    val optionThree: IntArray
        get() = InterfaceMappingRegistry.dialogueData().optionThree
    val optionFour: IntArray
        get() = InterfaceMappingRegistry.dialogueData().optionFour
    val optionFive: IntArray
        get() = InterfaceMappingRegistry.dialogueData().optionFive
    val toggleSpecialsButtons: IntArray
        get() = InterfaceMappingRegistry.dialogueData().toggleSpecialsButtons
    val toggleBossYellButtons: IntArray
        get() = InterfaceMappingRegistry.dialogueData().toggleBossYellButtons
}
