package net.dodian.uber.game.persistence.world

import net.dodian.uber.game.model.objects.WorldObject
import net.dodian.uber.game.objects.ObjectSpawnRepository

object ObjectDefinitionRepository {
    @JvmStatic
    fun loadObjects(): List<WorldObject> =
        ObjectSpawnRepository.load()
}