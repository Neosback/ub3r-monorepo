package net.dodian.uber.game.content.skills.farming

import net.dodian.uber.game.content.platform.FarmingPatchDefinition
import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.model.Position

object FarmingDefinitions {
    data class PatchType(
        val name: String,
        val ordinal: Int,
        val level: Int,
        val seed: Int,
        val plantXp: Int,
        val disease: Int,
        val config: Int,
        val stages: Int,
        val ticks: Int,
        val checkHealthXp: Int,
        val harvestXp: Int,
        val harvestItem: Int,
    ) {
        override fun toString(): String = name
    }

    data class SaplingType(
        val name: String,
        val ordinal: Int,
        val farmLevel: Int,
        val treeSeed: Int,
        val plantedId: Int,
        val waterId: Int,
        val saplingId: Int,
    ) {
        override fun toString(): String = name
    }

    data class PatchGroup(
        val name: String,
        val ordinal: Int,
        val updatePos: Position,
        val objectId: Array<Int>,
    ) {
        override fun toString(): String = name
    }

    class CompostType(
        val name: String,
        val ordinal: Int,
        val itemId: Int,
        val divideValue: Int,
    ) {
        override fun toString(): String = name
    }

    data class CompostBinType(
        val name: String,
        val ordinal: Int,
        val updatePos: Position,
        val objectId: Int,
        val ticks: Int,
    ) {
        override fun toString(): String = name
    }

    private fun data() = SkillDataRegistry.farmingData()

        private fun patchesByFamily(family: String): List<PatchType> =
            data().patchDefinitions
                .filter { it.family.equals(family, ignoreCase = true) }
                .sortedBy { it.ordinal }
                .map { it.toPatchType() }

        private fun FarmingPatchDefinition.toPatchType() =
            PatchType(
                name = name,
                ordinal = ordinal,
                level = level,
                seed = seed,
                plantXp = plantXp,
                disease = disease,
                config = config,
                stages = stages,
                ticks = ticks,
                checkHealthXp = checkHealthXp,
                harvestXp = harvestXp,
                harvestItem = harvestItem,
            )

    object allotmentPatch {
            fun values(): Array<PatchType> = patchesByFamily("ALLOTMENT").toTypedArray()
            fun find(id: Int): PatchType? = values().firstOrNull { it.seed == id }
            fun findSeed(id: Int): Boolean = find(id) != null
        }

    object flowerPatch {
            fun values(): Array<PatchType> = patchesByFamily("FLOWER").toTypedArray()
            fun find(id: Int): PatchType? = values().firstOrNull { it.seed == id }
            fun findSeed(id: Int): Boolean = find(id) != null
        }

    object herbPatch {
            fun values(): Array<PatchType> = patchesByFamily("HERB").toTypedArray()
            fun find(id: Int): PatchType? = values().firstOrNull { it.seed == id }
            fun findSeed(id: Int): Boolean = find(id) != null
        }

    object bushPatch {
            fun values(): Array<PatchType> = patchesByFamily("BUSH").toTypedArray()
            fun find(id: Int): PatchType? = values().firstOrNull { it.seed == id }
            fun findSeed(id: Int): Boolean = find(id) != null
        }

    object fruitTreePatch {
            fun values(): Array<PatchType> = patchesByFamily("FRUIT_TREE").toTypedArray()
            fun find(id: Int): PatchType? = values().firstOrNull { it.seed == id }
            fun findSeed(id: Int): Boolean = find(id) != null
        }

    object treePatch {
            fun values(): Array<PatchType> = patchesByFamily("TREE").toTypedArray()
            fun find(id: Int): PatchType? = values().firstOrNull { it.seed == id }
            fun findSeed(id: Int): Boolean = find(id) != null
        }

    object sapling {
            private fun all(): List<SaplingType> =
                data().saplings
                    .sortedBy { it.ordinal }
                    .map {
                        SaplingType(
                            name = it.name,
                            ordinal = it.ordinal,
                            farmLevel = it.farmLevel,
                            treeSeed = it.treeSeed,
                            plantedId = it.plantedId,
                            waterId = it.waterId,
                            saplingId = it.saplingId,
                        )
                    }

            fun values(): Array<SaplingType> = all().toTypedArray()
        }

    object patches {
            private fun all(): List<PatchGroup> =
                data().patchGroups
                    .sortedBy { it.ordinal }
                    .map {
                        PatchGroup(
                            name = it.name,
                            ordinal = it.ordinal,
                            updatePos = Position(it.updateX, it.updateY, it.updateZ),
                            objectId = it.objectIds.toTypedArray(),
                        )
                    }

            fun values(): Array<PatchGroup> = all().toTypedArray()
        }

    object patchState {
            const val WEED = "WEED"
            const val GROWING = "GROWING"
            const val PROTECTED = "PROTECTED"
            const val DISEASE = "DISEASE"
            const val DEAD = "DEAD"
            const val HARVEST = "HARVEST"
            const val WATER = "WATER"
            const val PRODUCTION = "PRODUCTION"
            const val STUMP = "STUMP"
        }

    object compost {
            private fun byName(): Map<String, CompostType> =
                data().compostTypes
                    .associate { row ->
                        row.name to CompostType(
                            name = row.name,
                            ordinal = row.ordinal,
                            itemId = row.itemId,
                            divideValue = row.divideValue,
                        )
                    }

            val NONE: CompostType
                get() = byName().getValue("NONE")
            val COMPOST: CompostType
                get() = byName().getValue("COMPOST")
            val SUPERCOMPOST: CompostType
                get() = byName().getValue("SUPERCOMPOST")
            val ULTRACOMPOST: CompostType
                get() = byName().getValue("ULTRACOMPOST")

            fun values(): Array<CompostType> = byName().values.sortedBy { it.ordinal }.toTypedArray()

            fun find(name: String): CompostType? = values().firstOrNull { it.name.equals(name, ignoreCase = true) }
        }

    object compostBin {
            private fun all(): List<CompostBinType> =
                data().compostBins
                    .sortedBy { it.ordinal }
                    .map {
                        CompostBinType(
                            name = it.name,
                            ordinal = it.ordinal,
                            updatePos = Position(it.updateX, it.updateY, it.updateZ),
                            objectId = it.objectId,
                            ticks = it.ticks,
                        )
                    }

            fun values(): Array<CompostBinType> = all().toTypedArray()
        }

    object compostState {
            const val EMPTY = "EMPTY"
            const val FILLED = "FILLED"
            const val CLOSED = "CLOSED"
            const val DONE = "DONE"
            const val OPEN = "OPEN"
        }
    private fun findAnyBySeed(id: Int): PatchType? {
        return allotmentPatch.find(id) ?: flowerPatch.find(id) ?: herbPatch.find(id) ?: bushPatch.find(id) ?: fruitTreePatch.find(id) ?: treePatch.find(id)
    }

    fun getPatchConfig(id: Int): Int = findAnyBySeed(id)?.config ?: 0

    fun getDiseaseChance(id: Int): Int = findAnyBySeed(id)?.disease ?: 0

    fun getGrowTick(id: Int): Int = findAnyBySeed(id)?.ticks ?: 0

    fun getEndStage(id: Int): Int = findAnyBySeed(id)?.stages ?: 0

    fun getLife(id: Int, compost: Int): Int = if (allotmentPatch.find(id) != null || herbPatch.find(id) != null) 3 + compost else 1

    fun getPlantLevel(id: Int): Int = findAnyBySeed(id)?.level ?: 0

    fun getPlantedXp(id: Int): Int = findAnyBySeed(id)?.plantXp ?: 0

    fun getHarvestXp(id: Int): Int = findAnyBySeed(id)?.harvestXp ?: 0

    fun getCheckHealthXp(id: Int): Int = findAnyBySeed(id)?.checkHealthXp ?: 0

    fun getHarvestItem(id: Int): Int = findAnyBySeed(id)?.harvestItem ?: 0

    fun getPlantName(id: Int): String = findAnyBySeed(id)?.name ?: ""

    fun canNote(id: Int, itemName: String): Boolean {
        val checkName = itemName.replace("grimy ", "")
        val matchesName = herbPatch.values().any { it.name.replace("_", " ").lowercase() == checkName }
        val matchesHarvest = sequenceOf(
            herbPatch.values().asList(),
            allotmentPatch.values().asList(),
            flowerPatch.values().asList(),
            bushPatch.values().asList(),
            fruitTreePatch.values().asList(),
            treePatch.values().asList(),
        ).flatten().any { it.harvestItem == id }
        return matchesName || matchesHarvest
    }

    fun checkSuperCompost(id: Int): Boolean = superCompostItems.indexOf(id) >= 0

    fun checkRegularCompost(id: Int): Boolean = regularCompostItems.indexOf(id) >= 0

    val regularCompostItems: List<Int>
        get() = data().regularCompostItems

    val superCompostItems: List<Int>
        get() = data().superCompostItems

    val BUCKET: Int
        get() = data().BUCKET
    val SPADE: Int
        get() = data().SPADE
    val RAKE: Int
        get() = data().RAKE
    val SEED_DIBBER: Int
        get() = data().SEED_DIBBER
    val TROWEL: Int
        get() = data().TROWEL
    val FILLED_PLANT_POT: Int
        get() = data().FILLED_PLANT_POT
    val EMPTY_PLANT_POT: Int
        get() = data().EMPTY_PLANT_POT
    val SECATEURS: Int
        get() = data().SECATEURS
    val MAGIC_SECATEURS: Int
        get() = data().MAGIC_SECATEURS
    val PLANT_CURE: Int
        get() = data().PLANT_CURE
    val VOLCANIC_ASH: Int
        get() = data().VOLCANIC_ASH

    val RAKE_ANIM: Int
        get() = data().RAKE_ANIM
    val SPADE_ANIM: Int
        get() = data().SPADE_ANIM
    val WATERCAN_ANIM: Int
        get() = data().WATERCAN_ANIM
    val PLANTSEED_ANIM: Int
        get() = data().PLANTSEED_ANIM
    val PRUNE_SECATEURS_ANIM: Int
        get() = data().PRUNE_SECATEURS_ANIM
    val HARVEST_FRUIT_ANIM: Int
        get() = data().HARVEST_FRUIT_ANIM
    val HARVEST_BUSH_ANIM: Int
        get() = data().HARVEST_BUSH_ANIM
    val HARVEST_ANIM: Int
        get() = data().HARVEST_ANIM
    val COMPOST_PATCH_ANIM: Int
        get() = data().COMPOST_PATCH_ANIM
    val CURING_ANIM: Int
        get() = data().CURING_ANIM
    val FILL_PLANTPOT_ANIM: Int
        get() = data().FILL_PLANTPOT_ANIM

    val farmPatchConfig: Int
        get() = data().farmPatchConfig
    val compostBinConfig: Int
        get() = data().compostBinConfig
}
