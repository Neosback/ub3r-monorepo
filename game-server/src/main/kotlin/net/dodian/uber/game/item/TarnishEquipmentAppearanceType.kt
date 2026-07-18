package net.dodian.uber.game.item

/** Controls which identity-kit body parts remain visible around equipped items. */
enum class TarnishEquipmentAppearanceType(
    val showHead: Boolean = true,
    val showBeard: Boolean = true,
    val showArms: Boolean = true,
) {
    DEFAULT,
    HAT,
    MASK(showBeard = false),
    HELM(showHead = false, showBeard = false),
    FACE(showHead = false),
    TORSO,
    BODY(showArms = false),
}
