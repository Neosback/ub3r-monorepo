package net.dodian.uber.game.engine.systems.pathing.collision

class CollisionManager(
    private val matrix: CollisionMatrix = CollisionMatrix(),
) {
    data class MatrixMetrics(
        val activeZones: Int,
        val payloadBytes: Long,
        val estimatedDirectoryBytes: Long,
    )

    fun matrixMetrics(): MatrixMetrics =
        MatrixMetrics(
            activeZones = matrix.activeZoneCount(),
            payloadBytes = matrix.zonePayloadBytes(),
            estimatedDirectoryBytes = matrix.estimatedDirectoryBytes(),
        )
    fun flagWall(x: Int, y: Int, z: Int, eastBlocked: Boolean) {
        if (eastBlocked) {
            wall(x, y, z, CollisionDirection.EAST)
        }
    }

    fun setFlag(x: Int, y: Int, z: Int, flag: Int) {
        matrix.flag(x, y, z, flag)
    }

    fun unsetFlag(x: Int, y: Int, z: Int, flag: Int) {
        matrix.clear(x, y, z, flag)
    }

    fun flagSolid(x: Int, y: Int, z: Int, impenetrable: Boolean = true) {
        markOccupant(z, x, y, 1, 1, impenetrable, add = true)
    }

    fun clearSolid(x: Int, y: Int, z: Int, impenetrable: Boolean = true) {
        markOccupant(z, x, y, 1, 1, impenetrable, add = false)
    }

    fun markOccupant(height: Int, x: Int, y: Int, width: Int, length: Int, impenetrable: Boolean, add: Boolean) {
        val flag = CollisionFlag.BLOCKED or if (impenetrable) CollisionFlag.IMPENETRABLE_BLOCKED else 0
        for (xPos in x until x + width) {
            for (yPos in y until y + length) {
                if (add) {
                    setFlag(xPos, yPos, height, flag)
                } else {
                    unsetFlag(xPos, yPos, height, flag)
                }
            }
        }
    }

    fun markBridge(height: Int, x: Int, y: Int) {
        setFlag(x, y, height, CollisionFlag.BRIDGE)
    }

    fun wall(x: Int, y: Int, z: Int, direction: CollisionDirection, impenetrable: Boolean = true) {
        markWall(direction, z, x, y, STRAIGHT_WALL, impenetrable)
    }

    fun clearWall(x: Int, y: Int, z: Int, direction: CollisionDirection, impenetrable: Boolean = true) {
        unmarkWall(direction, z, x, y, STRAIGHT_WALL, impenetrable)
    }

    fun largeCornerWall(x: Int, y: Int, z: Int, direction: CollisionDirection, impenetrable: Boolean = true) {
        markWall(direction, z, x, y, ENTIRE_WALL, impenetrable)
    }

    fun clearLargeCornerWall(x: Int, y: Int, z: Int, direction: CollisionDirection, impenetrable: Boolean = true) {
        unmarkWall(direction, z, x, y, ENTIRE_WALL, impenetrable)
    }

    fun markWall(orientation: CollisionDirection, height: Int, x: Int, y: Int, type: Int, impenetrable: Boolean) {
        when (type) {
            STRAIGHT_WALL -> markStraightWall(orientation, height, x, y, impenetrable, add = true)
            ENTIRE_WALL -> markEntireWall(orientation, height, x, y, impenetrable, add = true)
            DIAGONAL_CORNER_WALL, WALL_CORNER -> markDiagonalCornerWall(orientation, height, x, y, impenetrable, add = true)
        }
    }

    fun unmarkWall(orientation: CollisionDirection, height: Int, x: Int, y: Int, type: Int, impenetrable: Boolean) {
        when (type) {
            STRAIGHT_WALL -> markStraightWall(orientation, height, x, y, impenetrable, add = false)
            ENTIRE_WALL -> markEntireWall(orientation, height, x, y, impenetrable, add = false)
            DIAGONAL_CORNER_WALL, WALL_CORNER -> markDiagonalCornerWall(orientation, height, x, y, impenetrable, add = false)
        }
    }

    fun apply(update: CollisionUpdate) {
        matrix.apply(update)
    }

    fun clear() {
        matrix.clearAll()
    }

    fun traversable(x: Int, y: Int, z: Int, dx: Int, dy: Int): Boolean {
        val direction = CollisionDirection.fromDelta(dx, dy)
        if (direction == CollisionDirection.NONE) {
            return true
        }
        return isTraversable(z, x - dx, y - dy, direction, 1)
    }

    fun projectileTraversable(x: Int, y: Int, z: Int, dx: Int, dy: Int): Boolean {
        val direction = CollisionDirection.fromDelta(dx, dy)
        if (direction == CollisionDirection.NONE) {
            return true
        }
        return isTraversable(z, x - dx, y - dy, direction, impenetrable = true) &&
            isTraversable(z, x, y, direction.opposite(), impenetrable = true)
    }

    fun getFlags(x: Int, y: Int, z: Int): Int = matrix.getFlags(x, y, z)

    fun isTileBlocked(x: Int, y: Int, z: Int): Boolean = matrix.hasFlags(x, y, z, CollisionFlag.BLOCKED)

    fun isNpcEdgeTraversable(height: Int, startX: Int, startY: Int, direction: CollisionDirection): Boolean =
        when (direction) {
            CollisionDirection.NORTH -> isInactive(height, startX, startY + 1, CollisionFlag.WALL_SOUTH)
            CollisionDirection.SOUTH -> isInactive(height, startX, startY - 1, CollisionFlag.WALL_NORTH)
            CollisionDirection.EAST -> isInactive(height, startX + 1, startY, CollisionFlag.WALL_WEST)
            CollisionDirection.WEST -> isInactive(height, startX - 1, startY, CollisionFlag.WALL_EAST)
            CollisionDirection.NORTH_EAST ->
                isInactive(height, startX + 1, startY + 1, CollisionFlag.WALL_WEST or CollisionFlag.WALL_SOUTH or CollisionFlag.WALL_SOUTH_WEST) &&
                isInactive(height, startX + 1, startY, CollisionFlag.WALL_WEST) &&
                isInactive(height, startX, startY + 1, CollisionFlag.WALL_SOUTH)
            CollisionDirection.NORTH_WEST ->
                isInactive(height, startX - 1, startY + 1, CollisionFlag.WALL_EAST or CollisionFlag.WALL_SOUTH or CollisionFlag.WALL_SOUTH_EAST) &&
                isInactive(height, startX - 1, startY, CollisionFlag.WALL_EAST) &&
                isInactive(height, startX, startY + 1, CollisionFlag.WALL_SOUTH)
            CollisionDirection.SOUTH_EAST ->
                isInactive(height, startX + 1, startY - 1, CollisionFlag.WALL_WEST or CollisionFlag.WALL_NORTH or CollisionFlag.WALL_NORTH_WEST) &&
                isInactive(height, startX + 1, startY, CollisionFlag.WALL_WEST) &&
                isInactive(height, startX, startY - 1, CollisionFlag.WALL_NORTH)
            CollisionDirection.SOUTH_WEST ->
                isInactive(height, startX - 1, startY - 1, CollisionFlag.WALL_EAST or CollisionFlag.WALL_NORTH or CollisionFlag.WALL_NORTH_EAST) &&
                isInactive(height, startX - 1, startY, CollisionFlag.WALL_EAST) &&
                isInactive(height, startX, startY - 1, CollisionFlag.WALL_NORTH)
            CollisionDirection.NONE -> true
        }

    fun canMove(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        z: Int,
        xLength: Int,
        yLength: Int,
    ): Boolean {
        var currentX = startX
        var currentY = startY
        val size = maxOf(xLength, yLength)

        while (currentX != endX || currentY != endY) {
            val stepX = Integer.compare(endX, currentX)
            val stepY = Integer.compare(endY, currentY)
            val direction = CollisionDirection.fromDelta(stepX, stepY)
            if (direction == CollisionDirection.NONE) {
                return true
            }
            if (!isTraversable(z, currentX, currentY, direction, size)) {
                return false
            }
            currentX += stepX
            currentY += stepY
        }

        return true
    }

    fun isTraversable(height: Int, x: Int, y: Int, direction: CollisionDirection, size: Int): Boolean {
        if (size == 1) {
            return isTraversable(height, x, y, direction, impenetrable = false)
        }
        return when (direction) {
            CollisionDirection.NORTH -> {
                for (offset in 0 until size) {
                    if (!isTraversableNorth(height, x + offset, y + size - 1, impenetrable = false)) return false
                }
                true
            }
            CollisionDirection.SOUTH -> {
                for (offset in 0 until size) {
                    if (!isTraversableSouth(height, x + offset, y, impenetrable = false)) return false
                }
                true
            }
            CollisionDirection.EAST -> {
                for (offset in 0 until size) {
                    if (!isTraversableEast(height, x + size - 1, y + offset, impenetrable = false)) return false
                }
                true
            }
            CollisionDirection.WEST -> {
                for (offset in 0 until size) {
                    if (!isTraversableWest(height, x, y + offset, impenetrable = false)) return false
                }
                true
            }
            CollisionDirection.NORTH_EAST -> {
                for (offset in 0 until size) {
                    if (!isTraversableNorth(height, x + offset, y + size - 1, impenetrable = false)) return false
                    if (!isTraversableEast(height, x + size - 1, y + offset, impenetrable = false)) return false
                }
                isTraversableNorthEast(height, x + size - 1, y + size - 1, impenetrable = false)
            }
            CollisionDirection.NORTH_WEST -> {
                for (offset in 0 until size) {
                    if (!isTraversableNorth(height, x + offset, y + size - 1, impenetrable = false)) return false
                    if (!isTraversableWest(height, x, y + offset, impenetrable = false)) return false
                }
                isTraversableNorthWest(height, x, y + size - 1, impenetrable = false)
            }
            CollisionDirection.SOUTH_EAST -> {
                for (offset in 0 until size) {
                    if (!isTraversableSouth(height, x + offset, y, impenetrable = false)) return false
                    if (!isTraversableEast(height, x + size - 1, y + offset, impenetrable = false)) return false
                }
                isTraversableSouthEast(height, x + size - 1, y, impenetrable = false)
            }
            CollisionDirection.SOUTH_WEST -> {
                for (offset in 0 until size) {
                    if (!isTraversableSouth(height, x + offset, y, impenetrable = false)) return false
                    if (!isTraversableWest(height, x, y + offset, impenetrable = false)) return false
                }
                isTraversableSouthWest(height, x, y, impenetrable = false)
            }
            CollisionDirection.NONE -> true
        }
    }

    fun isTraversable(height: Int, x: Int, y: Int, direction: CollisionDirection, impenetrable: Boolean): Boolean =
        when (direction) {
            CollisionDirection.NORTH -> isTraversableNorth(height, x, y, impenetrable)
            CollisionDirection.SOUTH -> isTraversableSouth(height, x, y, impenetrable)
            CollisionDirection.EAST -> isTraversableEast(height, x, y, impenetrable)
            CollisionDirection.WEST -> isTraversableWest(height, x, y, impenetrable)
            CollisionDirection.NORTH_EAST -> isTraversableNorthEast(height, x, y, impenetrable)
            CollisionDirection.NORTH_WEST -> isTraversableNorthWest(height, x, y, impenetrable)
            CollisionDirection.SOUTH_EAST -> isTraversableSouthEast(height, x, y, impenetrable)
            CollisionDirection.SOUTH_WEST -> isTraversableSouthWest(height, x, y, impenetrable)
            CollisionDirection.NONE -> true
        }

    private fun markStraightWall(
        orientation: CollisionDirection,
        height: Int,
        x: Int,
        y: Int,
        impenetrable: Boolean,
        add: Boolean,
    ) {
        when (orientation) {
            CollisionDirection.WEST -> {
                update(height, x, y, CollisionFlag.WALL_WEST, add)
                update(height, x - 1, y, CollisionFlag.WALL_EAST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_WEST, add)
                    update(height, x - 1, y, CollisionFlag.IMPENETRABLE_WALL_EAST, add)
                }
            }
            CollisionDirection.NORTH -> {
                update(height, x, y, CollisionFlag.WALL_NORTH, add)
                update(height, x, y + 1, CollisionFlag.WALL_SOUTH, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_NORTH, add)
                    update(height, x, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH, add)
                }
            }
            CollisionDirection.EAST -> {
                update(height, x, y, CollisionFlag.WALL_EAST, add)
                update(height, x + 1, y, CollisionFlag.WALL_WEST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_EAST, add)
                    update(height, x + 1, y, CollisionFlag.IMPENETRABLE_WALL_WEST, add)
                }
            }
            CollisionDirection.SOUTH -> {
                update(height, x, y, CollisionFlag.WALL_SOUTH, add)
                update(height, x, y - 1, CollisionFlag.WALL_NORTH, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_SOUTH, add)
                    update(height, x, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH, add)
                }
            }
            else -> Unit
        }
    }

    private fun markEntireWall(
        orientation: CollisionDirection,
        height: Int,
        x: Int,
        y: Int,
        impenetrable: Boolean,
        add: Boolean,
    ) {
        when (orientation) {
            CollisionDirection.WEST -> {
                update(height, x, y, CollisionFlag.WALL_WEST or CollisionFlag.WALL_NORTH, add)
                update(height, x - 1, y, CollisionFlag.WALL_EAST, add)
                update(height, x, y + 1, CollisionFlag.WALL_SOUTH, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_WEST or CollisionFlag.IMPENETRABLE_WALL_NORTH, add)
                    update(height, x - 1, y, CollisionFlag.IMPENETRABLE_WALL_EAST, add)
                    update(height, x, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH, add)
                }
            }
            CollisionDirection.NORTH -> {
                update(height, x, y, CollisionFlag.WALL_EAST or CollisionFlag.WALL_NORTH, add)
                update(height, x, y + 1, CollisionFlag.WALL_SOUTH, add)
                update(height, x + 1, y, CollisionFlag.WALL_WEST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_EAST or CollisionFlag.IMPENETRABLE_WALL_NORTH, add)
                    update(height, x, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH, add)
                    update(height, x + 1, y, CollisionFlag.IMPENETRABLE_WALL_WEST, add)
                }
            }
            CollisionDirection.EAST -> {
                update(height, x, y, CollisionFlag.WALL_EAST or CollisionFlag.WALL_SOUTH, add)
                update(height, x + 1, y, CollisionFlag.WALL_WEST, add)
                update(height, x, y - 1, CollisionFlag.WALL_NORTH, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_EAST or CollisionFlag.IMPENETRABLE_WALL_SOUTH, add)
                    update(height, x + 1, y, CollisionFlag.IMPENETRABLE_WALL_WEST, add)
                    update(height, x, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH, add)
                }
            }
            CollisionDirection.SOUTH -> {
                update(height, x, y, CollisionFlag.WALL_WEST or CollisionFlag.WALL_SOUTH, add)
                update(height, x - 1, y, CollisionFlag.WALL_EAST, add)
                update(height, x, y - 1, CollisionFlag.WALL_NORTH, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_WEST or CollisionFlag.IMPENETRABLE_WALL_SOUTH, add)
                    update(height, x - 1, y, CollisionFlag.IMPENETRABLE_WALL_EAST, add)
                    update(height, x, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH, add)
                }
            }
            else -> Unit
        }
    }

    private fun markDiagonalCornerWall(
        orientation: CollisionDirection,
        height: Int,
        x: Int,
        y: Int,
        impenetrable: Boolean,
        add: Boolean,
    ) {
        when (orientation) {
            CollisionDirection.WEST -> {
                update(height, x, y, CollisionFlag.WALL_NORTH_WEST, add)
                update(height, x - 1, y + 1, CollisionFlag.WALL_SOUTH_EAST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_NORTH_WEST, add)
                    update(height, x - 1, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH_EAST, add)
                }
            }
            CollisionDirection.NORTH -> {
                update(height, x, y, CollisionFlag.WALL_NORTH_EAST, add)
                update(height, x + 1, y + 1, CollisionFlag.WALL_SOUTH_WEST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_NORTH_EAST, add)
                    update(height, x + 1, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH_WEST, add)
                }
            }
            CollisionDirection.EAST -> {
                update(height, x, y, CollisionFlag.WALL_SOUTH_EAST, add)
                update(height, x + 1, y - 1, CollisionFlag.WALL_NORTH_WEST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_SOUTH_EAST, add)
                    update(height, x + 1, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH_WEST, add)
                }
            }
            CollisionDirection.SOUTH -> {
                update(height, x, y, CollisionFlag.WALL_SOUTH_WEST, add)
                update(height, x - 1, y - 1, CollisionFlag.WALL_NORTH_EAST, add)
                if (impenetrable) {
                    update(height, x, y, CollisionFlag.IMPENETRABLE_WALL_SOUTH_WEST, add)
                    update(height, x - 1, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH_EAST, add)
                }
            }
            else -> Unit
        }
    }

    private fun update(height: Int, x: Int, y: Int, flag: Int, add: Boolean) {
        if (add) {
            setFlag(x, y, height, flag)
        } else {
            unsetFlag(x, y, height, flag)
        }
    }

    private fun isTraversableNorth(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(height, x, y + 1, CollisionFlag.IMPENETRABLE_BLOCKED or CollisionFlag.IMPENETRABLE_WALL_SOUTH)
        } else {
            isInactive(height, x, y + 1, CollisionFlag.WALL_SOUTH or CollisionFlag.BLOCKED)
        }

    private fun isTraversableSouth(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(height, x, y - 1, CollisionFlag.IMPENETRABLE_BLOCKED or CollisionFlag.IMPENETRABLE_WALL_NORTH)
        } else {
            isInactive(height, x, y - 1, CollisionFlag.WALL_NORTH or CollisionFlag.BLOCKED)
        }

    private fun isTraversableEast(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(height, x + 1, y, CollisionFlag.IMPENETRABLE_BLOCKED or CollisionFlag.IMPENETRABLE_WALL_WEST)
        } else {
            isInactive(height, x + 1, y, CollisionFlag.WALL_WEST or CollisionFlag.BLOCKED)
        }

    private fun isTraversableWest(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(height, x - 1, y, CollisionFlag.IMPENETRABLE_BLOCKED or CollisionFlag.IMPENETRABLE_WALL_EAST)
        } else {
            isInactive(height, x - 1, y, CollisionFlag.WALL_EAST or CollisionFlag.BLOCKED)
        }

    private fun isTraversableNorthEast(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(
                height,
                x + 1,
                y + 1,
                CollisionFlag.IMPENETRABLE_WALL_WEST or CollisionFlag.IMPENETRABLE_WALL_SOUTH or CollisionFlag.IMPENETRABLE_WALL_SOUTH_WEST,
            ) &&
                isInactive(height, x + 1, y, CollisionFlag.IMPENETRABLE_WALL_WEST or CollisionFlag.IMPENETRABLE_BLOCKED) &&
                isInactive(height, x, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH or CollisionFlag.IMPENETRABLE_BLOCKED)
        } else {
            isInactive(
                height,
                x + 1,
                y + 1,
                CollisionFlag.WALL_WEST or CollisionFlag.WALL_SOUTH or CollisionFlag.WALL_SOUTH_WEST or CollisionFlag.BLOCKED,
            ) &&
                isInactive(height, x + 1, y, CollisionFlag.WALL_WEST or CollisionFlag.BLOCKED) &&
                isInactive(height, x, y + 1, CollisionFlag.WALL_SOUTH or CollisionFlag.BLOCKED)
        }

    private fun isTraversableNorthWest(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(
                height,
                x - 1,
                y + 1,
                CollisionFlag.IMPENETRABLE_WALL_EAST or CollisionFlag.IMPENETRABLE_WALL_SOUTH or CollisionFlag.IMPENETRABLE_WALL_SOUTH_EAST,
            ) &&
                isInactive(height, x - 1, y, CollisionFlag.IMPENETRABLE_WALL_EAST or CollisionFlag.IMPENETRABLE_BLOCKED) &&
                isInactive(height, x, y + 1, CollisionFlag.IMPENETRABLE_WALL_SOUTH or CollisionFlag.IMPENETRABLE_BLOCKED)
        } else {
            isInactive(
                height,
                x - 1,
                y + 1,
                CollisionFlag.WALL_EAST or CollisionFlag.WALL_SOUTH or CollisionFlag.WALL_SOUTH_EAST or CollisionFlag.BLOCKED,
            ) &&
                isInactive(height, x - 1, y, CollisionFlag.WALL_EAST or CollisionFlag.BLOCKED) &&
                isInactive(height, x, y + 1, CollisionFlag.WALL_SOUTH or CollisionFlag.BLOCKED)
        }

    private fun isTraversableSouthEast(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(
                height,
                x + 1,
                y - 1,
                CollisionFlag.IMPENETRABLE_WALL_WEST or CollisionFlag.IMPENETRABLE_WALL_NORTH or CollisionFlag.IMPENETRABLE_WALL_NORTH_WEST,
            ) &&
                isInactive(height, x + 1, y, CollisionFlag.IMPENETRABLE_WALL_WEST or CollisionFlag.IMPENETRABLE_BLOCKED) &&
                isInactive(height, x, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH or CollisionFlag.IMPENETRABLE_BLOCKED)
        } else {
            isInactive(
                height,
                x + 1,
                y - 1,
                CollisionFlag.WALL_WEST or CollisionFlag.WALL_NORTH or CollisionFlag.WALL_NORTH_WEST or CollisionFlag.BLOCKED,
            ) &&
                isInactive(height, x + 1, y, CollisionFlag.WALL_WEST or CollisionFlag.BLOCKED) &&
                isInactive(height, x, y - 1, CollisionFlag.WALL_NORTH or CollisionFlag.BLOCKED)
        }

    private fun isTraversableSouthWest(height: Int, x: Int, y: Int, impenetrable: Boolean): Boolean =
        if (impenetrable) {
            isInactive(
                height,
                x - 1,
                y - 1,
                CollisionFlag.IMPENETRABLE_WALL_EAST or CollisionFlag.IMPENETRABLE_WALL_NORTH or CollisionFlag.IMPENETRABLE_WALL_NORTH_EAST,
            ) &&
                isInactive(height, x - 1, y, CollisionFlag.IMPENETRABLE_WALL_EAST or CollisionFlag.IMPENETRABLE_BLOCKED) &&
                isInactive(height, x, y - 1, CollisionFlag.IMPENETRABLE_WALL_NORTH or CollisionFlag.IMPENETRABLE_BLOCKED)
        } else {
            isInactive(
                height,
                x - 1,
                y - 1,
                CollisionFlag.WALL_EAST or CollisionFlag.WALL_NORTH or CollisionFlag.WALL_NORTH_EAST or CollisionFlag.BLOCKED,
            ) &&
                isInactive(height, x - 1, y, CollisionFlag.WALL_EAST or CollisionFlag.BLOCKED) &&
                isInactive(height, x, y - 1, CollisionFlag.WALL_NORTH or CollisionFlag.BLOCKED)
        }

    private fun isInactive(height: Int, x: Int, y: Int, flag: Int): Boolean =
        matrix.getFlags(x, y, height) and flag == 0

    companion object {
        private const val STRAIGHT_WALL = 0
        private const val DIAGONAL_CORNER_WALL = 1
        private const val ENTIRE_WALL = 2
        private const val WALL_CORNER = 3

        private val GLOBAL = CollisionManager()

        @JvmStatic
        fun global(): CollisionManager = GLOBAL
    }
}
