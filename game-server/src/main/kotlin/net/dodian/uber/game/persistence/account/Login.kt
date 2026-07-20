package net.dodian.uber.game.persistence.account

import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.sql.SQLException
import net.dodian.uber.game.persistence.db.DbTables
import net.dodian.uber.game.persistence.repository.DbAsyncRepository
import net.dodian.uber.game.engine.config.gameWorldId
import org.slf4j.LoggerFactory

import net.dodian.uber.game.persistence.DbDispatchers

class Login {
    private val logger = LoggerFactory.getLogger(Login::class.java)

    fun sendSession(
        dbId: Int,
        clientPid: Int,
        elapsed: Int,
        connectedFrom: String,
        start: Long,
        end: Long,
    ) {
        DbDispatchers.accountExecutor.execute {
            try {
                DbAsyncRepository.withConnection { conn ->
                    val query =
                        "INSERT INTO ${DbTables.GAME_PLAYER_SESSIONS} (dbid, client, duration, hostname, start, end, world) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    conn.prepareStatement(query).use { statement ->
                        statement.setInt(1, dbId)
                        statement.setInt(2, clientPid)
                        statement.setInt(3, elapsed)
                        statement.setString(4, connectedFrom)
                        statement.setLong(5, start)
                        statement.setLong(6, end)
                        statement.setInt(7, gameWorldId)
                        statement.executeUpdate()
                    }
                }
            } catch (exception: SQLException) {
                logger.error("Failed to record player session for dbId={}", dbId, exception)
            } catch (exception: RuntimeException) {
                logger.error("Failed to record player session for dbId={}", dbId, exception)
            }
        }
    }

    companion object {
        @JvmField
        val bannedUid: MutableSet<String> = LinkedHashSet()

        @JvmStatic
        fun isUidBanned(UUID: Array<String>?): Boolean {
            if (UUID == null) {
                return false
            }
            for (value in UUID) {
                if (isUidBanned(value)) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun isUidBanned(UUID: String?): Boolean {
            if (UUID.isNullOrBlank()) {
                return false
            }
            return bannedUid.contains(UUID)
        }

        @JvmStatic
        fun banUid() {
            // UUIDBans.txt has been deprecated and removed. A database-backed ban system will be implemented later.
        }

        @JvmStatic
        fun addUidToFile(UUID: Array<String>?) {
            if (UUID == null || UUID.isEmpty()) {
                return
            }
            for (value in UUID) {
                if (value.isNotBlank() && !isUidBanned(value)) {
                    bannedUid.add(value)
                }
            }
        }

        private val logger = LoggerFactory.getLogger(Login::class.java)
    }
}