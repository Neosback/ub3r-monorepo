package net.dodian.game.persistence.world

import net.dodian.game.model.`object`.RS2Object
import net.dodian.game.persistence.db.DbTables
import net.dodian.game.persistence.repository.DbAsyncRepository

object ObjectDefinitionRepository {
    @JvmStatic
    fun loadObjects(): List<RS2Object> =
        DbAsyncRepository.withConnection { connection ->
            connection
                .createStatement()
                .use { statement ->
                    statement.executeQuery("SELECT id, x, y, type FROM ${DbTables.GAME_OBJECT_DEFINITIONS}").use { results ->
                        val objects = ArrayList<RS2Object>()
                        while (results.next()) {
                            objects += RS2Object(results.getInt("id"), results.getInt("x"), results.getInt("y"), results.getInt("type"))
                        }
                        objects
                    }
                }
        }
}
