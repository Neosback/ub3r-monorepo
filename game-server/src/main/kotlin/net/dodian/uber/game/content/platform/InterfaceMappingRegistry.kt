package net.dodian.uber.game.content.platform

import net.dodian.uber.game.content.interfaces.magic.MagicComponents
import net.dodian.uber.game.content.interfaces.skillguide.SkillGuideComponents

data class MagicDataFile(
    val spellbookToggleButtons: IntArray = intArrayOf(),
    val autocastClearButtons: IntArray = intArrayOf(),
    val autocastSelectButtons: IntArray = intArrayOf(),
    val autocastRefreshButtons: IntArray = intArrayOf(),
    val teleports: List<MagicComponents.TeleportBinding> = emptyList(),
)

data class SkillGuideDataFile(
    val skillButtons: List<SkillGuideComponents.SkillGuideButtonGroup> = emptyList(),
    val subTabs: List<SkillGuideComponents.SubTabDefinition> = emptyList(),
    val baselineHidden: IntArray = intArrayOf(),
    val baselineShown: IntArray = intArrayOf(),
    val titleComponentIds: IntArray = intArrayOf(),
)

data class TravelObjectsDataFile(
    val passageObjects: IntArray = intArrayOf(),
    val teleportObjects: IntArray = intArrayOf(),
    val webObstacleObjects: IntArray = intArrayOf(),
)

data class UiComponentsDataFile(
    val runOffButtons: IntArray = intArrayOf(),
    val runOnButtons: IntArray = intArrayOf(),
    val runToggleButtons: IntArray = intArrayOf(),
    val tabInterfaceDefaultButtons: IntArray = intArrayOf(),
    val tabInterfaceEquipmentButtons: IntArray = intArrayOf(),
    val sidebarHomeButtons: IntArray = intArrayOf(),
    val closeInterfaceButtons: IntArray = intArrayOf(),
    val questTabToggleButtons: IntArray = intArrayOf(),
    val logoutButtons: IntArray = intArrayOf(),
    val morphButtons: IntArray = intArrayOf(),
    val ignoredButtons: IntArray = intArrayOf(),
)

data class DialogueComponentsDataFile(
    val optionOne: IntArray = intArrayOf(),
    val optionTwo: IntArray = intArrayOf(),
    val optionThree: IntArray = intArrayOf(),
    val optionFour: IntArray = intArrayOf(),
    val optionFive: IntArray = intArrayOf(),
    val toggleSpecialsButtons: IntArray = intArrayOf(),
    val toggleBossYellButtons: IntArray = intArrayOf(),
)

data class BankComponentsDataFile(
    val depositInventoryButtons: IntArray = intArrayOf(),
    val depositWornItemsButtons: IntArray = intArrayOf(),
    val withdrawAsNoteButtons: IntArray = intArrayOf(),
    val withdrawAsItemButtons: IntArray = intArrayOf(),
    val searchButtons: IntArray = intArrayOf(),
    val tabButtons: IntArray = intArrayOf(),
)

data class SettingsComponentsDataFile(
    val openMoreSettingsButtons: IntArray = intArrayOf(),
    val closeMoreSettingsButtons: IntArray = intArrayOf(),
    val pinHelpButtons: IntArray = intArrayOf(),
    val bossYellEnableButtons: IntArray = intArrayOf(),
    val bossYellDisableButtons: IntArray = intArrayOf(),
)

data class EmoteComponentsDataFile(
    val goblinBowButtons: IntArray = intArrayOf(),
    val goblinSaluteButtons: IntArray = intArrayOf(),
    val glassBoxButtons: IntArray = intArrayOf(),
    val climbRopeButtons: IntArray = intArrayOf(),
    val leanButtons: IntArray = intArrayOf(),
    val glassWallButtons: IntArray = intArrayOf(),
    val ideaButtons: IntArray = intArrayOf(),
    val stompButtons: IntArray = intArrayOf(),
    val skillcapeButtons: IntArray = intArrayOf(),
)

data class DuelRuleButtonIndex(
    val buttonId: Int,
    val ruleIndex: Int,
)

data class DuelComponentsDataFile(
    val offerRuleButtons: IntArray = intArrayOf(),
    val offerRuleIndices: List<DuelRuleButtonIndex> = emptyList(),
    val bodyRuleButtons: IntArray = intArrayOf(),
)

data class PartyRoomComponentsDataFile(
    val depositAcceptButtons: IntArray = intArrayOf(),
)

data class SlotsComponentsDataFile(
    val spinButtons: IntArray = intArrayOf(),
)

data class AppearanceComponentsDataFile(
    val confirmButtons: IntArray = intArrayOf(),
)

data class RewardComponentsDataFile(
    val skillSelectionButtons: IntArray = intArrayOf(),
)

data class BankingObjectsDataFile(
    val boothObjects: IntArray = intArrayOf(),
    val chestObjects: IntArray = intArrayOf(),
)

data class PartyRoomObjectsDataFile(
    val balloonObjects: IntArray = intArrayOf(),
    val depositChest: Int = -1,
    val forceTrigger: Int = -1,
)

object InterfaceMappingRegistry {
    @Volatile
    private var magicData: MagicDataFile? = null

    @Volatile
    private var skillGuideData: SkillGuideDataFile? = null

    @Volatile
    private var travelData: TravelObjectsDataFile? = null

    @Volatile
    private var uiData: UiComponentsDataFile? = null

    @Volatile
    private var dialogueData: DialogueComponentsDataFile? = null

    @Volatile
    private var bankData: BankComponentsDataFile? = null

    @Volatile
    private var settingsData: SettingsComponentsDataFile? = null

    @Volatile
    private var emoteData: EmoteComponentsDataFile? = null

    @Volatile
    private var duelData: DuelComponentsDataFile? = null

    @Volatile
    private var partyRoomData: PartyRoomComponentsDataFile? = null

    @Volatile
    private var slotsData: SlotsComponentsDataFile? = null

    @Volatile
    private var appearanceData: AppearanceComponentsDataFile? = null

    @Volatile
    private var rewardData: RewardComponentsDataFile? = null

    @Volatile
    private var bankingObjectsData: BankingObjectsDataFile? = null

    @Volatile
    private var partyRoomObjectsData: PartyRoomObjectsDataFile? = null

    @JvmStatic
    fun magicData(): MagicDataFile {
        val loaded = magicData ?: ContentDataLoader.loadRequired<MagicDataFile>("content/interfaces/magic.toml").also {
            magicData = it
        }
        return loaded
    }

    @JvmStatic
    fun skillGuideData(): SkillGuideDataFile {
        val loaded = skillGuideData ?: ContentDataLoader.loadRequired<SkillGuideDataFile>("content/interfaces/skillguide.toml").also {
            skillGuideData = it
        }
        return loaded
    }

    @JvmStatic
    fun travelData(): TravelObjectsDataFile {
        val loaded = travelData ?: ContentDataLoader.loadRequired<TravelObjectsDataFile>("content/objects/travel.toml").also {
            travelData = it
        }
        return loaded
    }

    @JvmStatic
    fun uiData(): UiComponentsDataFile {
        val loaded = uiData ?: ContentDataLoader.loadRequired<UiComponentsDataFile>("content/interfaces/ui.toml").also {
            uiData = it
        }
        return loaded
    }

    @JvmStatic
    fun dialogueData(): DialogueComponentsDataFile {
        val loaded = dialogueData ?: ContentDataLoader.loadRequired<DialogueComponentsDataFile>("content/interfaces/dialogue.toml").also {
            dialogueData = it
        }
        return loaded
    }

    @JvmStatic
    fun bankData(): BankComponentsDataFile {
        val loaded = bankData ?: ContentDataLoader.loadRequired<BankComponentsDataFile>("content/interfaces/bank.toml").also {
            bankData = it
        }
        return loaded
    }

    @JvmStatic
    fun settingsData(): SettingsComponentsDataFile {
        val loaded = settingsData ?: ContentDataLoader.loadRequired<SettingsComponentsDataFile>("content/interfaces/settings.toml").also {
            settingsData = it
        }
        return loaded
    }

    @JvmStatic
    fun emoteData(): EmoteComponentsDataFile {
        val loaded = emoteData ?: ContentDataLoader.loadRequired<EmoteComponentsDataFile>("content/interfaces/emotes.toml").also {
            emoteData = it
        }
        return loaded
    }

    @JvmStatic
    fun duelData(): DuelComponentsDataFile {
        val loaded = duelData ?: ContentDataLoader.loadRequired<DuelComponentsDataFile>("content/interfaces/duel.toml").also {
            duelData = it
        }
        return loaded
    }

    @JvmStatic
    fun partyRoomData(): PartyRoomComponentsDataFile {
        val loaded = partyRoomData ?: ContentDataLoader.loadRequired<PartyRoomComponentsDataFile>("content/interfaces/partyroom.toml").also {
            partyRoomData = it
        }
        return loaded
    }

    @JvmStatic
    fun slotsData(): SlotsComponentsDataFile {
        val loaded = slotsData ?: ContentDataLoader.loadRequired<SlotsComponentsDataFile>("content/interfaces/slots.toml").also {
            slotsData = it
        }
        return loaded
    }

    @JvmStatic
    fun appearanceData(): AppearanceComponentsDataFile {
        val loaded = appearanceData ?: ContentDataLoader.loadRequired<AppearanceComponentsDataFile>("content/interfaces/appearance.toml").also {
            appearanceData = it
        }
        return loaded
    }

    @JvmStatic
    fun rewardData(): RewardComponentsDataFile {
        val loaded = rewardData ?: ContentDataLoader.loadRequired<RewardComponentsDataFile>("content/interfaces/rewards.toml").also {
            rewardData = it
        }
        return loaded
    }

    @JvmStatic
    fun bankingObjectsData(): BankingObjectsDataFile {
        val loaded = bankingObjectsData ?: ContentDataLoader.loadRequired<BankingObjectsDataFile>("content/objects/banking.toml").also {
            bankingObjectsData = it
        }
        return loaded
    }

    @JvmStatic
    fun partyRoomObjectsData(): PartyRoomObjectsDataFile {
        val loaded = partyRoomObjectsData ?: ContentDataLoader.loadRequired<PartyRoomObjectsDataFile>("content/objects/partyroom.toml").also {
            partyRoomObjectsData = it
        }
        return loaded
    }
}
