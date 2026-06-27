package net.dodian.uber.game.engine.systems.pathing.collision

object CollisionFlag {
    const val NONE: Int = 0x0
    const val BRIDGE: Int = 0x40000
    const val BLOCKED: Int = 0x200000

    const val WALL_NORTH: Int = 0x2
    const val WALL_SOUTH: Int = 0x20
    const val WALL_EAST: Int = 0x8
    const val WALL_WEST: Int = 0x80
    const val WALL_NORTH_EAST: Int = 0x4
    const val WALL_NORTH_WEST: Int = 0x1
    const val WALL_SOUTH_EAST: Int = 0x10
    const val WALL_SOUTH_WEST: Int = 0x40

    const val IMPENETRABLE_BLOCKED: Int = 0x20000
    const val IMPENETRABLE_WALL_NORTH: Int = 0x400
    const val IMPENETRABLE_WALL_SOUTH: Int = 0x4000
    const val IMPENETRABLE_WALL_EAST: Int = 0x1000
    const val IMPENETRABLE_WALL_WEST: Int = 0x10000
    const val IMPENETRABLE_WALL_NORTH_EAST: Int = 0x800
    const val IMPENETRABLE_WALL_NORTH_WEST: Int = 0x200
    const val IMPENETRABLE_WALL_SOUTH_EAST: Int = 0x2000
    const val IMPENETRABLE_WALL_SOUTH_WEST: Int = 0x8000

    fun fullTile(impenetrable: Boolean = true): Int =
        BLOCKED or if (impenetrable) IMPENETRABLE_BLOCKED else 0
}

enum class CollisionDirection(
    val dx: Int,
    val dy: Int,
) {
    NORTH_WEST(-1, 1),
    NORTH(0, 1),
    NORTH_EAST(1, 1),
    WEST(-1, 0),
    EAST(1, 0),
    SOUTH_WEST(-1, -1),
    SOUTH(0, -1),
    SOUTH_EAST(1, -1),
    NONE(0, 0),
    ;

    fun opposite(): CollisionDirection =
        when (this) {
            NORTH_WEST -> SOUTH_EAST
            NORTH -> SOUTH
            NORTH_EAST -> SOUTH_WEST
            WEST -> EAST
            EAST -> WEST
            SOUTH_WEST -> NORTH_EAST
            SOUTH -> NORTH
            SOUTH_EAST -> NORTH_WEST
            NONE -> NONE
        }

    fun isDiagonal(): Boolean = dx != 0 && dy != 0

    companion object {
        val WNES: List<CollisionDirection> = listOf(WEST, NORTH, EAST, SOUTH)

        fun fromDelta(dx: Int, dy: Int): CollisionDirection =
            values().firstOrNull { it.dx == dx && it.dy == dy } ?: NONE
    }
}
