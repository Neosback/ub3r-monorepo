package net.dodian.uber.game.npc

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dodian.uber.game.engine.loop.GameThreadIngress
import net.dodian.uber.game.engine.loop.GameThreadTimers
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.persistence.DbDispatchers
import net.dodian.uber.game.persistence.repository.DbAsyncRepository
import org.slf4j.LoggerFactory
import java.util.LinkedHashMap

/**
 * Periodically refreshes a snapshot of the maximum XP value for each skill
 * across all characters in the database. The cache is updated asynchronously
 * on a background dispatcher and swapped atomically so readers always see
 * a consistent snapshot without any per-request database work.
 */
object LegendsGuardRankingCache {
    private val logger = LoggerFactory.getLogger(LegendsGuardRankingCache::class.java)

    @Volatile
    private var topXpBySkill: Map<Skill, Int> = emptyMap()

    @JvmStatic
    fun topXp(skill: Skill): Int = topXpBySkill[skill] ?: 0

    @JvmStatic
    @JvmOverloads
    fun start(refreshIntervalMinutes: Long = 5) {
        refresh()
        GameThreadTimers.schedule(
            "legends-ranking-cache-refresh",
            refreshIntervalMinutes * 60_000L,
        ) {
            refresh()
        }
    }

    private fun refresh() {
        GlobalScope.launch(DbDispatchers.accountDispatcher) {
            try {
                val snapshot = DbAsyncRepository.withConnection { conn ->
                    val columns = StringBuilder()
                    for (skill in Skill.enabledSkills()) {
                        if (columns.isNotEmpty()) columns.append(", ")
                        columns.append("MAX(${skill.getName()}) AS ${skill.getName()}")
                    }

                    val stmt = conn.prepareStatement("SELECT $columns FROM character_stats")
                    val rs = stmt.executeQuery()
                    val values = LinkedHashMap<Skill, Int>()
                    if (rs.next()) {
                        for (skill in Skill.enabledSkills()) {
                            val xp = rs.getInt(skill.getName())
                            if (!rs.wasNull()) {
                                values[skill] = xp
                            }
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    (values as Map<Skill, Int>)
                }

                GameThreadIngress.submitCritical("ranking-cache-swap") {
                    topXpBySkill = snapshot
                    logger.debug(
                        "Ranking cache refreshed: {} skills top_sample={}",
                        snapshot.size,
                        snapshot.entries.joinToString(limit = 3) { "${it.key.getName()}=${it.value}" },
                    )
                }
            } catch (exception: Exception) {
                logger.warn("Failed to refresh legends ranking cache", exception)
            }
        }
    }
}
