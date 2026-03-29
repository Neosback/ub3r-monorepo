package net.dodian.util

import net.dodian.util.math.Geometry
import net.dodian.util.math.Randoms
import net.dodian.util.text.Formatting
import net.dodian.util.text.Names
import net.dodian.util.text.Text

/**
 * Facade object providing backward compatibility for Java code.
 * 
 * Provides @JvmStatic methods and @JvmField exports that delegate to domain-specific utility classes:
 * - Name operations → util.text.Names
 * - Text operations → util.text.Text
 * - Formatting → util.text.Formatting
 * - Random operations → util.math.Randoms
 * - Geometry calculations → util.math.Geometry
 * - Direction calculations → util.Direction
 * 
 * @see net.dodian.util.text.Names
 * @see net.dodian.util.text.Text
 * @see net.dodian.util.text.Formatting
 * @see net.dodian.util.math.Randoms
 * @see net.dodian.util.math.Geometry
 * @see net.dodian.util.Direction
 * 
 * TODO: Phase 3/4 - Consider deprecating in favor of direct usage of domain utilities
 * TODO: See MIGRATION_NOTES.md for migration strategy
 */
object Utils {
    @JvmField val playerNameXlateTable: CharArray = Names.playerNameXlateTable
    @JvmField val directionDeltaX: ByteArray = Direction.directionDeltaX
    @JvmField val directionDeltaY: ByteArray = Direction.directionDeltaY
    @JvmField val xlateDirectionToClient: ByteArray = Direction.xlateDirectionToClient
    @JvmField val xlateTable: CharArray = Text.xlateTable

    @JvmStatic
    fun longToPlayerName(l: Long): String = Names.longToPlayerName(l)

    @JvmStatic
    fun println(str: String) = Formatting.println(str)

    @JvmStatic
    fun HexToInt(
        data: ByteArray,
        offset: Int,
        len: Int,
    ): Int = Text.hexToInt(data, offset, len)

    @JvmStatic
    fun random(range: Int): Int = Randoms.random(range)

    @JvmStatic
    fun dRandom2(range: Double): Double = Randoms.dRandom2(range)

    @JvmStatic
    fun playerNameToInt64(s: String): Long = Names.playerNameToInt64(s)

    @JvmStatic
    fun playerNameToLong(s: String): Long = Names.playerNameToLong(s)

    @JvmStatic
    fun textUnpack(
        packedData: ByteArray,
        size: Int,
    ): String = Text.textUnpack(packedData, size)

    @JvmStatic
    fun direction(
        srcX: Int,
        srcY: Int,
        destX: Int,
        destY: Int,
    ): Int = Direction.direction(srcX, srcY, destX, destY)

    @JvmStatic
    fun format(num: Int): String = Formatting.format(num)

    @JvmStatic
    fun getDistance(
        coordX1: Int,
        coordY1: Int,
        coordX2: Int,
        coordY2: Int,
    ): Int = Geometry.getDistance(coordX1, coordY1, coordX2, coordY2)

    @JvmStatic
    fun println_debug(message: String) = Formatting.printlnDebug(message)

    @JvmStatic
    fun capitalize(str: String?): String? = Formatting.capitalize(str)
}
