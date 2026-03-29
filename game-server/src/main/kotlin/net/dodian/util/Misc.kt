package net.dodian.util

import net.dodian.cache.`object`.GameObjectDef
import net.dodian.game.model.Position
import net.dodian.util.math.Geometry
import net.dodian.util.math.Randoms
import net.dodian.util.text.Formatting

/**
 * Facade object providing backward compatibility for Java code.
 * 
 * All methods delegate to domain-specific utility classes:
 * - Random operations → util.math.Randoms
 * - Formatting → util.text.Formatting
 * - Geometry calculations → util.math.Geometry
 * 
 * @see net.dodian.util.math.Randoms
 * @see net.dodian.util.text.Formatting
 * @see net.dodian.util.math.Geometry
 * 
 * TODO: Phase 3/4 - Consider deprecating in favor of direct usage of domain utilities
 * TODO: See MIGRATION_NOTES.md for migration strategy
 */
object Misc {
    @JvmStatic
    fun random(range: Int): Int = Randoms.random(range)

    @JvmStatic
    fun chance(range: Int): Int = Randoms.chance(range)

    @JvmStatic
    fun format(num: Int): String = Formatting.format(num)

    @JvmStatic
    fun goodDistanceObject(
        objectX: Int,
        objectY: Int,
        playerX: Int,
        playerY: Int,
        objectXSize: Int,
        objectYSize: Int,
        z: Int,
    ): Position? = Geometry.goodDistanceObject(objectX, objectY, playerX, playerY, objectXSize, objectYSize, z)

    @JvmStatic
    fun goodDistanceObject(
        objectX: Int,
        objectY: Int,
        playerX: Int,
        playerY: Int,
        distance: Int,
        z: Int,
    ): Position? = Geometry.goodDistanceObject(objectX, objectY, playerX, playerY, distance, z)

    @JvmStatic
    fun delta(
        a: Position,
        b: Position,
    ): Position = Geometry.delta(a, b)

    @JvmStatic
    fun getObject(
        objectId: Int,
        x: Int,
        y: Int,
        h: Int,
    ): GameObjectDef? = Geometry.getObject(objectId, x, y, h)
}
