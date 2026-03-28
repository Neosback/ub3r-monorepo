package net.dodian.uber.game.content.platform

import net.dodian.uber.game.content.skills.cooking.CookingDefinition
import net.dodian.uber.game.content.skills.crafting.GemDefinition
import net.dodian.uber.game.content.skills.crafting.GoldJewelryDefinitionData
import net.dodian.uber.game.content.skills.crafting.GoldJewelryGroupData
import net.dodian.uber.game.content.skills.crafting.HideDefinition
import net.dodian.uber.game.content.skills.crafting.OrbDefinition
import net.dodian.uber.game.content.skills.crafting.StandardLeatherCraftDefinition
import net.dodian.uber.game.content.skills.crafting.TanningDefinition
import net.dodian.uber.game.content.skills.fishing.FishingSpotDefinition
import net.dodian.uber.game.content.skills.fletching.ArrowRecipe
import net.dodian.uber.game.content.skills.fletching.DartRecipe
import net.dodian.uber.game.content.skills.fletching.FletchingLogDefinition
import net.dodian.uber.game.content.skills.herblore.HerbDefinition
import net.dodian.uber.game.content.skills.herblore.PotionDoseDefinition
import net.dodian.uber.game.content.skills.herblore.PotionRecipeDefinition
import net.dodian.uber.game.content.skills.mining.MiningRockDef
import net.dodian.uber.game.content.skills.mining.PickaxeDef
import net.dodian.uber.game.content.skills.prayer.PrayerBoneDefinition
import net.dodian.uber.game.content.skills.runecrafting.RunecraftingAltarDefinition
import net.dodian.uber.game.content.skills.slayer.SlayerTaskDefinition
import net.dodian.uber.game.content.skills.smithing.OreRequirement
import net.dodian.uber.game.content.skills.thieving.ThievingDefinition
import net.dodian.uber.game.content.skills.woodcutting.AxeDef
import net.dodian.uber.game.content.skills.woodcutting.TreeDef

private data class CookingDataFile(
    val recipes: List<CookingDefinition> = emptyList(),
)

private data class FishingDataFile(
    val fishingSpots: List<FishingSpotDefinition> = emptyList(),
)

private data class FletchingDataFile(
    val bowLogs: List<FletchingLogDefinition> = emptyList(),
    val arrowRecipes: List<ArrowRecipe> = emptyList(),
    val dartRecipes: List<DartRecipe> = emptyList(),
    val extraBowWeaponIds: List<Int> = emptyList(),
)

private data class WoodcuttingDataFile(
    val trees: List<TreeDef> = emptyList(),
    val axes: List<AxeDef> = emptyList(),
)

private data class MiningDataFile(
    val rocks: List<MiningRockDef> = emptyList(),
    val pickaxes: List<PickaxeDef> = emptyList(),
    val randomGemDropTable: List<Int> = emptyList(),
)

private data class CraftingDataFile(
    val hideDefinitions: List<HideDefinition> = emptyList(),
    val gemDefinitions: List<GemDefinition> = emptyList(),
    val orbDefinitions: List<OrbDefinition> = emptyList(),
    val standardLeatherCrafts: List<StandardLeatherCraftDefinition> = emptyList(),
    val tanningDefinitions: List<TanningDefinition> = emptyList(),
    val goldJewelry: GoldJewelryDefinitionData = GoldJewelryDefinitionData(),
    val resourceFillSources: List<ResourceFillSourceDefinition> = emptyList(),
)

private data class HerbloreDataFile(
    val herbDefinitions: List<HerbDefinition> = emptyList(),
    val potionRecipes: List<PotionRecipeDefinition> = emptyList(),
    val potionDoseDefinitions: List<PotionDoseDefinition> = emptyList(),
)

private data class RunecraftingDataFile(
    val runeEssenceId: Int = 1436,
    val altarDefinitions: List<RunecraftingAltarDefinition> = emptyList(),
)

private data class SlayerDataFile(
    val mazchna: List<String> = emptyList(),
    val vannaka: List<String> = emptyList(),
    val duradel: List<String> = emptyList(),
)

private data class PrayerDataFile(
    val altarObjectIds: List<Int> = emptyList(),
    val bones: List<PrayerBoneDefinition> = emptyList(),
)

data class SmithingPageSlot(
    val frameId: Int,
    val indices: List<Int> = emptyList(),
)

data class FillItemDefinition(
    val emptyItemId: Int,
    val filledItemId: Int,
    val emote: Int,
)

data class ResourceFillSourceDefinition(
    val sourceKey: String,
    val objectIds: List<Int> = emptyList(),
    val entries: List<FillItemDefinition> = emptyList(),
)

data class SmithingLayoutSlot(
    val slotType: String,
    val itemNameLineId: Int,
    val barCountLineId: Int,
    val barsRequired: Int,
)

data class SmeltingButtonGroup(
    val amount: Int,
    val rawButtonIds: List<Int> = emptyList(),
)

data class SmeltingRecipeV2(
    val barId: Int,
    val name: String,
    val levelRequired: Int,
    val experience: Int,
    val oreRequirements: List<OreRequirement> = emptyList(),
    val buttonGroups: List<SmeltingButtonGroup> = emptyList(),
    val successChancePercent: Int = 100,
    val failureMessage: String? = null,
)

data class SmithingTierItemData(
    val itemId: Int,
    val levelRequired: Int,
    val outputAmount: Int = 1,
)

data class SmithingTierData(
    val typeId: Int,
    val displayName: String,
    val barId: Int,
    val products: List<SmithingTierItemData> = emptyList(),
)

data class SmithingDataFile(
    val smeltFrameIds: List<Int> = emptyList(),
    val smithingPageSlots: List<SmithingPageSlot> = emptyList(),
    val smithingLayout: List<SmithingLayoutSlot> = emptyList(),
    val smeltingRecipes: List<SmeltingRecipeV2> = emptyList(),
    val smithingTiers: List<SmithingTierData> = emptyList(),
)

private data class ThievingDataFile(
    val definitions: List<ThievingDefinition> = emptyList(),
    val stallObjects: List<Int> = emptyList(),
    val chestObjects: List<Int> = emptyList(),
    val plunderObjects: List<Int> = emptyList(),
    val specialChests: List<ThievingSpecialChestDefinition> = emptyList(),
    val plunder: PyramidPlunderData = PyramidPlunderData(),
)

data class PositionData(
    val x: Int,
    val y: Int,
    val z: Int = 0,
)

data class ThievingSpecialChestReward(
    val itemId: Int,
)

data class ThievingSpecialChestDefinition(
    val chestKey: String,
    val objectId: Int,
    val location: PositionData,
    val requiredLevel: Int,
    val premiumOnly: Boolean = false,
    val lockMs: Long = 1200L,
    val replacementObjectId: Int = 378,
    val replacementLifetimeMs: Int = 12000,
    val replacementType: Int = 10,
    val replacementFace: Int = 2,
    val rareRewardChancePercent: Double = 0.3,
    val rareRewards: List<ThievingSpecialChestReward> = emptyList(),
    val coinsMin: Int,
    val coinsMaxOffset: Int,
    val baseXp: Int,
    val randomEventXp: Int,
)

data class PyramidPlunderData(
    val allDoors: List<PositionData> = emptyList(),
    val roomEntrances: List<PositionData> = emptyList(),
    val start: PositionData = PositionData(1927, 4477, 0),
    val end: PositionData = PositionData(3289, 2801, 0),
    val obstacles: List<Int> = emptyList(),
    val urnConfig: List<Int> = emptyList(),
    val tombConfig: List<Int> = emptyList(),
)

private data class CraftingObjectComponentsDataFile(
    val resourceFillObjects: List<Int> = emptyList(),
    val spinningWheelObjects: List<Int> = emptyList(),
)

private data class CookingObjectComponentsDataFile(
    val rangeObjects: List<Int> = emptyList(),
)

private data class SmithingObjectComponentsDataFile(
    val anvilObjects: List<Int> = emptyList(),
    val furnaceObjects: List<Int> = emptyList(),
    val smeltingInterfaceFurnaces: List<Int> = emptyList(),
)

private data class FarmingObjectComponentsDataFile(
    val patchGuideObjects: List<Int> = emptyList(),
)

private data class RunecraftingObjectComponentsDataFile(
    val altarObjects: List<Int> = emptyList(),
)

data class FarmingPatchDefinition(
    val name: String,
    val family: String,
    val ordinal: Int,
    val level: Int,
    val seed: Int,
    val plantXp: Int,
    val disease: Int,
    val config: Int,
    val stages: Int,
    val ticks: Int,
    val checkHealthXp: Int = 0,
    val harvestXp: Int,
    val harvestItem: Int,
)

data class FarmingSaplingDefinition(
    val name: String,
    val ordinal: Int,
    val farmLevel: Int,
    val treeSeed: Int,
    val plantedId: Int,
    val waterId: Int,
    val saplingId: Int,
)

data class FarmingPatchGroup(
    val name: String,
    val ordinal: Int,
    val updateX: Int,
    val updateY: Int,
    val updateZ: Int,
    val objectIds: List<Int> = emptyList(),
)

data class FarmingCompostType(
    val name: String,
    val ordinal: Int,
    val itemId: Int,
    val divideValue: Int,
)

data class FarmingCompostBin(
    val name: String,
    val ordinal: Int,
    val updateX: Int,
    val updateY: Int,
    val updateZ: Int,
    val objectId: Int,
    val ticks: Int,
)

data class FarmingDataFile(
    val patchDefinitions: List<FarmingPatchDefinition> = emptyList(),
    val saplings: List<FarmingSaplingDefinition> = emptyList(),
    val patchGroups: List<FarmingPatchGroup> = emptyList(),
    val compostTypes: List<FarmingCompostType> = emptyList(),
    val compostBins: List<FarmingCompostBin> = emptyList(),
    val regularCompostItems: List<Int> = emptyList(),
    val superCompostItems: List<Int> = emptyList(),
    val BUCKET: Int = 1925,
    val SPADE: Int = 952,
    val RAKE: Int = 5341,
    val SEED_DIBBER: Int = 5343,
    val TROWEL: Int = 5325,
    val FILLED_PLANT_POT: Int = 5354,
    val EMPTY_PLANT_POT: Int = 5350,
    val SECATEURS: Int = 5329,
    val MAGIC_SECATEURS: Int = 7409,
    val PLANT_CURE: Int = 6036,
    val VOLCANIC_ASH: Int = 21622,
    val RAKE_ANIM: Int = 2273,
    val SPADE_ANIM: Int = 830,
    val WATERCAN_ANIM: Int = 2293,
    val PLANTSEED_ANIM: Int = 2291,
    val PRUNE_SECATEURS_ANIM: Int = 2279,
    val HARVEST_FRUIT_ANIM: Int = 2280,
    val HARVEST_BUSH_ANIM: Int = 2281,
    val HARVEST_ANIM: Int = 2282,
    val COMPOST_PATCH_ANIM: Int = 2283,
    val CURING_ANIM: Int = 2288,
    val FILL_PLANTPOT_ANIM: Int = 2287,
    val farmPatchConfig: Int = 4771,
    val compostBinConfig: Int = 4775,
    val patchStates: List<String> = emptyList(),
    val compostStates: List<String> = emptyList(),
)

object SkillDataRegistry {
    object MigrationStatus {
        val required: Set<String> = setOf(
            "cooking",
            "fishing",
            "fletching",
            "woodcutting",
            "mining",
            "crafting",
            "herblore",
            "runecrafting",
            "slayer",
            "prayer",
            "smithing",
            "farming",
            "thieving",
        )
        val pending: Set<String> = emptySet()
    }

    @Volatile
    private var cookingOverride: CookingDataFile? = null

    @Volatile
    private var fishingOverride: FishingDataFile? = null

    @Volatile
    private var fletchingOverride: FletchingDataFile? = null

    @Volatile
    private var woodcuttingOverride: WoodcuttingDataFile? = null

    @Volatile
    private var miningOverride: MiningDataFile? = null

    @Volatile
    private var craftingOverride: CraftingDataFile? = null

    @Volatile
    private var herbloreOverride: HerbloreDataFile? = null

    @Volatile
    private var runecraftingOverride: RunecraftingDataFile? = null

    @Volatile
    private var slayerOverride: SlayerDataFile? = null

    @Volatile
    private var prayerOverride: PrayerDataFile? = null

    @Volatile
    private var smithingOverride: SmithingDataFile? = null

    @Volatile
    private var farmingOverride: FarmingDataFile? = null

    @Volatile
    private var thievingOverride: ThievingDataFile? = null

    @Volatile
    private var craftingObjectComponentsOverride: CraftingObjectComponentsDataFile? = null

    @Volatile
    private var cookingObjectComponentsOverride: CookingObjectComponentsDataFile? = null

    @Volatile
    private var smithingObjectComponentsOverride: SmithingObjectComponentsDataFile? = null

    @Volatile
    private var farmingObjectComponentsOverride: FarmingObjectComponentsDataFile? = null

    @Volatile
    private var runecraftingObjectComponentsOverride: RunecraftingObjectComponentsDataFile? = null

    @JvmStatic
    fun cookingRecipes(): List<CookingDefinition> =
        (cookingOverride ?: ContentDataLoader.loadRequired<CookingDataFile>("content/skills/cooking.toml").also { cookingOverride = it }).recipes

    @JvmStatic
    fun fishingSpots(): List<FishingSpotDefinition> =
        (fishingOverride ?: ContentDataLoader.loadRequired<FishingDataFile>("content/skills/fishing.toml").also { fishingOverride = it }).fishingSpots

    @JvmStatic
    fun fletchingBowLogs(): List<FletchingLogDefinition> =
        (fletchingOverride ?: ContentDataLoader.loadRequired<FletchingDataFile>("content/skills/fletching.toml").also { fletchingOverride = it }).bowLogs

    @JvmStatic
    fun fletchingArrowRecipes(): List<ArrowRecipe> =
        (fletchingOverride ?: ContentDataLoader.loadRequired<FletchingDataFile>("content/skills/fletching.toml").also { fletchingOverride = it }).arrowRecipes

    @JvmStatic
    fun fletchingDartRecipes(): List<DartRecipe> =
        (fletchingOverride ?: ContentDataLoader.loadRequired<FletchingDataFile>("content/skills/fletching.toml").also { fletchingOverride = it }).dartRecipes

    @JvmStatic
    fun fletchingExtraBowWeaponIds(): Set<Int> =
        (fletchingOverride ?: ContentDataLoader.loadRequired<FletchingDataFile>("content/skills/fletching.toml").also { fletchingOverride = it }).extraBowWeaponIds.toSet()

    @JvmStatic
    fun woodcuttingTrees(): List<TreeDef> =
        (woodcuttingOverride ?: ContentDataLoader.loadRequired<WoodcuttingDataFile>("content/skills/woodcutting.toml").also { woodcuttingOverride = it }).trees

    @JvmStatic
    fun woodcuttingAxes(): List<AxeDef> =
        (woodcuttingOverride ?: ContentDataLoader.loadRequired<WoodcuttingDataFile>("content/skills/woodcutting.toml").also { woodcuttingOverride = it }).axes

    @JvmStatic
    fun miningRocks(): List<MiningRockDef> =
        (miningOverride ?: ContentDataLoader.loadRequired<MiningDataFile>("content/skills/mining.toml").also { miningOverride = it }).rocks

    @JvmStatic
    fun miningPickaxes(): List<PickaxeDef> =
        (miningOverride ?: ContentDataLoader.loadRequired<MiningDataFile>("content/skills/mining.toml").also { miningOverride = it }).pickaxes

    @JvmStatic
    fun miningRandomGemDropTable(): IntArray =
        (miningOverride ?: ContentDataLoader.loadRequired<MiningDataFile>("content/skills/mining.toml").also { miningOverride = it }).randomGemDropTable.toIntArray()

    @JvmStatic
    fun craftingHideDefinitions(): List<HideDefinition> =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).hideDefinitions

    @JvmStatic
    fun craftingGemDefinitions(): List<GemDefinition> =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).gemDefinitions

    @JvmStatic
    fun craftingOrbDefinitions(): List<OrbDefinition> =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).orbDefinitions

    @JvmStatic
    fun craftingStandardLeatherCrafts(): List<StandardLeatherCraftDefinition> =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).standardLeatherCrafts

    @JvmStatic
    fun craftingTanningDefinitions(): List<TanningDefinition> =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).tanningDefinitions

    @JvmStatic
    fun craftingGoldJewelryDefinition(): GoldJewelryDefinitionData =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).goldJewelry

    @JvmStatic
    fun craftingResourceFillSources(): List<ResourceFillSourceDefinition> =
        (craftingOverride ?: ContentDataLoader.loadRequired<CraftingDataFile>("content/skills/crafting.toml").also { craftingOverride = it }).resourceFillSources

    @JvmStatic
    fun herbloreHerbDefinitions(): List<HerbDefinition> =
        (herbloreOverride ?: ContentDataLoader.loadRequired<HerbloreDataFile>("content/skills/herblore.toml").also { herbloreOverride = it }).herbDefinitions

    @JvmStatic
    fun herblorePotionRecipes(): List<PotionRecipeDefinition> =
        (herbloreOverride ?: ContentDataLoader.loadRequired<HerbloreDataFile>("content/skills/herblore.toml").also { herbloreOverride = it }).potionRecipes

    @JvmStatic
    fun herblorePotionDoseDefinitions(): List<PotionDoseDefinition> =
        (herbloreOverride ?: ContentDataLoader.loadRequired<HerbloreDataFile>("content/skills/herblore.toml").also { herbloreOverride = it }).potionDoseDefinitions

    @JvmStatic
    fun runecraftingRuneEssenceId(): Int =
        (runecraftingOverride ?: ContentDataLoader.loadRequired<RunecraftingDataFile>("content/skills/runecrafting.toml").also { runecraftingOverride = it }).runeEssenceId

    @JvmStatic
    fun runecraftingAltars(): List<RunecraftingAltarDefinition> =
        (runecraftingOverride ?: ContentDataLoader.loadRequired<RunecraftingDataFile>("content/skills/runecrafting.toml").also { runecraftingOverride = it }).altarDefinitions

    @JvmStatic
    fun slayerMazchnaTasks(): Array<SlayerTaskDefinition> =
        resolveTasks((slayerOverride ?: ContentDataLoader.loadRequired<SlayerDataFile>("content/skills/slayer.toml").also { slayerOverride = it }).mazchna)

    @JvmStatic
    fun slayerVannakaTasks(): Array<SlayerTaskDefinition> =
        resolveTasks((slayerOverride ?: ContentDataLoader.loadRequired<SlayerDataFile>("content/skills/slayer.toml").also { slayerOverride = it }).vannaka)

    @JvmStatic
    fun slayerDuradelTasks(): Array<SlayerTaskDefinition> =
        resolveTasks((slayerOverride ?: ContentDataLoader.loadRequired<SlayerDataFile>("content/skills/slayer.toml").also { slayerOverride = it }).duradel)

    @JvmStatic
    fun prayerAltarObjectIds(): IntArray =
        (prayerOverride ?: ContentDataLoader.loadRequired<PrayerDataFile>("content/skills/prayer.toml").also { prayerOverride = it }).altarObjectIds.toIntArray()

    @JvmStatic
    fun prayerBones(): List<PrayerBoneDefinition> =
        (prayerOverride ?: ContentDataLoader.loadRequired<PrayerDataFile>("content/skills/prayer.toml").also { prayerOverride = it }).bones

    @JvmStatic
    fun smithingData(): SmithingDataFile =
        smithingOverride ?: ContentDataLoader.loadRequired<SmithingDataFile>("content/skills/smithing.toml").also { smithingOverride = it }

    @JvmStatic
    fun farmingData(): FarmingDataFile =
        farmingOverride ?: ContentDataLoader.loadRequired<FarmingDataFile>("content/skills/farming.toml").also { farmingOverride = it }

    @JvmStatic
    fun thievingDefinitions(): List<ThievingDefinition> =
        (thievingOverride ?: ContentDataLoader.loadRequired<ThievingDataFile>("content/skills/thieving.toml").also { thievingOverride = it }).definitions

    @JvmStatic
    fun thievingStallObjects(): IntArray =
        (thievingOverride ?: ContentDataLoader.loadRequired<ThievingDataFile>("content/skills/thieving.toml").also { thievingOverride = it }).stallObjects.toIntArray()

    @JvmStatic
    fun thievingChestObjects(): IntArray =
        (thievingOverride ?: ContentDataLoader.loadRequired<ThievingDataFile>("content/skills/thieving.toml").also { thievingOverride = it }).chestObjects.toIntArray()

    @JvmStatic
    fun thievingPlunderObjects(): IntArray =
        (thievingOverride ?: ContentDataLoader.loadRequired<ThievingDataFile>("content/skills/thieving.toml").also { thievingOverride = it }).plunderObjects.toIntArray()

    @JvmStatic
    fun thievingSpecialChests(): List<ThievingSpecialChestDefinition> =
        (thievingOverride ?: ContentDataLoader.loadRequired<ThievingDataFile>("content/skills/thieving.toml").also { thievingOverride = it }).specialChests

    @JvmStatic
    fun thievingPlunderData(): PyramidPlunderData =
        (thievingOverride ?: ContentDataLoader.loadRequired<ThievingDataFile>("content/skills/thieving.toml").also { thievingOverride = it }).plunder

    @JvmStatic
    fun craftingResourceFillObjects(): IntArray =
        (craftingObjectComponentsOverride ?: ContentDataLoader.loadRequired<CraftingObjectComponentsDataFile>("content/skills/objects/crafting.toml").also { craftingObjectComponentsOverride = it }).resourceFillObjects.toIntArray()

    @JvmStatic
    fun craftingSpinningWheelObjects(): IntArray =
        (craftingObjectComponentsOverride ?: ContentDataLoader.loadRequired<CraftingObjectComponentsDataFile>("content/skills/objects/crafting.toml").also { craftingObjectComponentsOverride = it }).spinningWheelObjects.toIntArray()

    @JvmStatic
    fun cookingRangeObjects(): IntArray =
        (cookingObjectComponentsOverride ?: ContentDataLoader.loadRequired<CookingObjectComponentsDataFile>("content/skills/objects/cooking.toml").also { cookingObjectComponentsOverride = it }).rangeObjects.toIntArray()

    @JvmStatic
    fun smithingAnvilObjects(): IntArray =
        (smithingObjectComponentsOverride ?: ContentDataLoader.loadRequired<SmithingObjectComponentsDataFile>("content/skills/objects/smithing.toml").also { smithingObjectComponentsOverride = it }).anvilObjects.toIntArray()

    @JvmStatic
    fun smithingFurnaceObjects(): IntArray =
        (smithingObjectComponentsOverride ?: ContentDataLoader.loadRequired<SmithingObjectComponentsDataFile>("content/skills/objects/smithing.toml").also { smithingObjectComponentsOverride = it }).furnaceObjects.toIntArray()

    @JvmStatic
    fun smithingSmeltingInterfaceFurnaces(): IntArray =
        (smithingObjectComponentsOverride ?: ContentDataLoader.loadRequired<SmithingObjectComponentsDataFile>("content/skills/objects/smithing.toml").also { smithingObjectComponentsOverride = it }).smeltingInterfaceFurnaces.toIntArray()

    @JvmStatic
    fun farmingPatchGuideObjects(): IntArray =
        (farmingObjectComponentsOverride ?: ContentDataLoader.loadRequired<FarmingObjectComponentsDataFile>("content/skills/objects/farming.toml").also { farmingObjectComponentsOverride = it }).patchGuideObjects.toIntArray()

    @JvmStatic
    fun runecraftingAltarObjects(): IntArray =
        (runecraftingObjectComponentsOverride ?: ContentDataLoader.loadRequired<RunecraftingObjectComponentsDataFile>("content/skills/objects/runecrafting.toml").also { runecraftingObjectComponentsOverride = it }).altarObjects.toIntArray()

    private fun resolveTasks(rawNames: List<String>): Array<SlayerTaskDefinition> =
        rawNames.map { SlayerTaskDefinition.valueOf(it) }.toTypedArray()
}
