package net.dodian.uber.skills.api

/** Marks nested skill builders so an author cannot accidentally use an outer DSL scope. */
@DslMarker
annotation class SkillDsl

data class SkillModuleDescriptor(
    val id: String,
    val displayName: String,
    val version: String = "1.0.0",
) {
    init {
        require(ID.matches(id)) { "Skill module id must be lowercase dot-separated: $id" }
        require(displayName.isNotBlank()) { "Skill module display name cannot be blank" }
        require(version.isNotBlank()) { "Skill module version cannot be blank" }
    }

    companion object {
        private val ID = Regex("skill\\.[a-z][a-z0-9-]{1,63}")
    }
}

data class SkillMaterial(val itemId: Int, val amount: Int = 1) {
    init {
        require(itemId >= 0) { "Material item id must be non-negative" }
        require(amount > 0) { "Material amount must be positive" }
    }
}

data class SkillRecipe(
    val outputItemId: Int,
    val outputAmount: Int = 1,
    val materials: List<SkillMaterial> = emptyList(),
) {
    init {
        require(outputItemId >= 0) { "Output item id must be non-negative" }
        require(outputAmount > 0) { "Output amount must be positive" }
        require(materials.groupBy { it.itemId }.all { (_, values) -> values.size == 1 }) {
            "A recipe must combine duplicate material ids before declaration"
        }
    }
}

@SkillDsl
class SkillRecipeBuilder(private val outputItemId: Int) {
    private var outputAmount: Int = 1
    private val materials = linkedMapOf<Int, Int>()

    fun output(amount: Int) {
        require(amount > 0) { "Output amount must be positive" }
        outputAmount = amount
    }

    fun material(itemId: Int, amount: Int = 1) {
        require(itemId >= 0) { "Material item id must be non-negative" }
        require(amount > 0) { "Material amount must be positive" }
        materials[itemId] = (materials[itemId] ?: 0) + amount
    }

    internal fun build(): SkillRecipe = SkillRecipe(
        outputItemId = outputItemId,
        outputAmount = outputAmount,
        materials = materials.map { (itemId, amount) -> SkillMaterial(itemId, amount) },
    )
}

fun skillRecipe(outputItemId: Int, block: SkillRecipeBuilder.() -> Unit = {}): SkillRecipe =
    SkillRecipeBuilder(outputItemId).apply(block).build()
