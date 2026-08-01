package net.dodian.uber.game.rscm

object Namer {

    fun sanitizeRSCM(value: String): String {
        var sanitized = removeTags(value)
            .replace(Regex("@[a-zA-Z0-9]{3}@"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')

        if (sanitized.isEmpty()) {
            return "unnamed"
        }

        return sanitized
    }

    fun removeTags(str: String): String {
        val builder = StringBuilder(str.length)
        var inTag = false

        for (char in str) {
            when (char) {
                '<' -> inTag = true
                '>' -> inTag = false
                else -> if (!inTag) builder.append(char)
            }
        }

        return builder.toString()
    }
}
