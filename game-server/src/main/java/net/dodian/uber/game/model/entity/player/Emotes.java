package net.dodian.uber.game.model.entity.player;

/**
 * @author Ryan Augustynowicz
 */
public enum Emotes {

    // Standard emotes — button IDs from dump ROOT 147 (new Tarnish client).
    // ANGRY/BECKON were swapped (old: ANGRY=165, BECKON=167; new client has 165=Beckon, 167=Angry).
    // Newer emotes (JIG–SHRUG) previously used prestige-skill button range 52050–52058 — now fixed.
    // GOBLIN_BOW, GOBLIN_DANCE, GLASS_BOX, CLIMB_ROPE, LEAN, GLASS_WALL removed from enum;
    //   each has a dedicated EmoteInterface binding with the correct newer animation ID.
    YES(168, 0x357), NO(169, 0x358), THINK(162, 0x359), BOW(164, 0x35A), ANGRY(167, 0x35B), CRY(161, 0x35C), LAUGH(170,
            0x35D), CHEER(171, 0x35E), WAVE(163, 0x35F), BECKON(165, 0x360), CLAP(172, 0x361), DANCE(166, 920),
    // Newer emotes: old buttons 52050–52058, 43092; new buttons from ROOT 147
    PANIC(13362, 0x839), JIG(13363, 0x83A), SPIN(13364, 0x83B), HEADBANG(13365, 0x83C), JUMP_FOR_JOY(13366,
            0x83D), RASP_BERRY(13367, 0x83E), YAWN(13368, 0x83F), SALUTE(13369, 0x840), SHRUG(13370,
            0x841), BLOW_KISS(11100, 0x558);

    private final int buttonId, animationId;

    Emotes(int buttonId, int animationId) {
        this.buttonId = buttonId;
        this.animationId = animationId;
    }

    public int getButtonId() {
        return buttonId;
    }

    public int getAnimationId() {
        return animationId;
    }

    public static void doEmote(int buttonId, Player player) {
        for (Emotes emote : Emotes.values()) {
            if (emote.getButtonId() == buttonId) {
                player.sendAnimation(emote.getAnimationId());
                return;
            }
        }
    }

}