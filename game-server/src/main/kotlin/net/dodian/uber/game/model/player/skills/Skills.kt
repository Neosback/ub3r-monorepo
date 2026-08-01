package net.dodian.uber.game.model.player.skills

object Skills {
    private const val MAX_LEVEL = 99

    /**
     * XP required to reach each level. Index zero is level one, so it also
     * preserves the legacy level-one requirement of zero XP.
     */
    private val xpForLevel =
        IntArray(MAX_LEVEL).apply {
            var points = 0.0
            for (level in 1 until size) {
                points += kotlin.math.floor(level + 300.0 * Math.pow(2.0, level.toDouble() / 7.0))
                this[level] = kotlin.math.floor(points / 4).toInt()
            }
        }

    @JvmStatic
    fun getLevelForExperience(exp: Int): Int {
        val safeExp = exp.coerceAtLeast(0)
        val searchResult = xpForLevel.binarySearch(safeExp)
        return if (searchResult >= 0) {
            searchResult + 1
        } else {
            (-searchResult - 1).coerceIn(1, MAX_LEVEL)
        }
    }

    @JvmStatic
    fun getXPForLevel(level: Int): Int {
        if (level in 1..MAX_LEVEL) {
            return xpForLevel[level - 1]
        }

        // Preserve the legacy behavior for callers outside the trainable
        // level range instead of silently changing an admin/tooling request.
        var points = 0.0
        var output = 0
        for (lvl in 1 until level) {
            points += kotlin.math.floor(lvl + 300.0 * Math.pow(2.0, lvl.toDouble() / 7.0))
            output = kotlin.math.floor(points / 4).toInt()
        }
        return output
    }

    @JvmStatic
    fun maxTotalLevel(): Int {
        var enabledCount = 0
        var disabledCount = 0
        for (skill in Skill.values()) {
            if (skill.isEnabled()) enabledCount++ else disabledCount++
        }
        return (enabledCount * 99) + disabledCount
    }
}
