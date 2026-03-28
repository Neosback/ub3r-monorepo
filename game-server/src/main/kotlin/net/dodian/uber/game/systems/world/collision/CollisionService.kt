package net.dodian.uber.game.systems.world.collision

import org.rsmod.routefinder.LineRouteFinding
import org.rsmod.routefinder.LineValidator
import org.rsmod.routefinder.StepValidator
import org.rsmod.routefinder.collision.CollisionFlagMap
import java.util.concurrent.atomic.AtomicReference

object CollisionService {
    private val flagsRef = AtomicReference(CollisionFlagMap())
    private val stepValidatorRef = AtomicReference(StepValidator(flagsRef.get()))
    private val lineWalkRef = AtomicReference(LineRouteFinding(flagsRef.get()))
    private val lineSightRef = AtomicReference(LineValidator(flagsRef.get()))

    @JvmStatic
    fun reset() {
        val fresh = CollisionFlagMap()
        flagsRef.set(fresh)
        stepValidatorRef.set(StepValidator(fresh))
        lineWalkRef.set(LineRouteFinding(fresh))
        lineSightRef.set(LineValidator(fresh))
    }

    @JvmStatic
    fun addFlag(x: Int, y: Int, level: Int, mask: Int) {
        flagsRef.get().add(x, y, level, mask)
    }

    @JvmStatic
    fun removeFlag(x: Int, y: Int, level: Int, mask: Int) {
        flagsRef.get().remove(x, y, level, mask)
    }

    @JvmStatic
    fun setFlag(x: Int, y: Int, level: Int, mask: Int) {
        flagsRef.get().set(x, y, level, mask)
    }

    @JvmStatic
    fun canMove(startX: Int, startY: Int, endX: Int, endY: Int, level: Int, xLength: Int, yLength: Int): Boolean {
        var currentX = startX
        var currentY = startY
        var diffX = endX - startX
        var diffY = endY - startY
        val max = kotlin.math.max(kotlin.math.abs(diffX), kotlin.math.abs(diffY))
        if (max == 0) {
            return true
        }
        val size = kotlin.math.max(1, kotlin.math.max(xLength, yLength))
        repeat(max) {
            val stepX =
                when {
                    diffX < 0 -> -1
                    diffX > 0 -> 1
                    else -> 0
                }
            val stepY =
                when {
                    diffY < 0 -> -1
                    diffY > 0 -> 1
                    else -> 0
                }
            if (!stepValidatorRef.get().canTravel(level, currentX, currentY, stepX, stepY, size)) {
                return false
            }
            currentX += stepX
            currentY += stepY
            diffX -= stepX
            diffY -= stepY
        }
        return true
    }

    @JvmStatic
    fun canNpcRoamStep(startX: Int, startY: Int, dx: Int, dy: Int, level: Int, size: Int): Boolean {
        if (dx == 0 && dy == 0) {
            return true
        }
        return stepValidatorRef.get().canTravel(level, startX, startY, dx, dy, kotlin.math.max(size, 1))
    }

    @JvmStatic
    fun hasLineOfWalk(level: Int, srcX: Int, srcY: Int, dstX: Int, dstY: Int): Boolean =
        lineWalkRef.get().lineOfWalk(level, srcX, srcY, dstX, dstY).success

    @JvmStatic
    fun hasLineOfSight(level: Int, srcX: Int, srcY: Int, dstX: Int, dstY: Int): Boolean =
        lineSightRef.get().hasLineOfSight(level, srcX, srcY, dstX, dstY)
}
