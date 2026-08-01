package net.dodian.uber.game.engine.systems.cache

class CollisionPlaneResolver private constructor(private val lowered: Array<Array<BooleanArray>>) {
    fun terrainPlane(localX: Int, localY: Int, plane: Int): Int =
        if (has(localX, localY, 1)) plane - 1 else plane

    fun objectPlane(localX: Int, localY: Int, plane: Int): Int =
        when {
            has(localX, localY, 1) -> plane - 1
            has(localX, localY, plane) -> plane - 1
            else -> plane
        }

    private fun has(localX: Int, localY: Int, plane: Int): Boolean =
        plane in 0 until MAP_PLANES && localX in 0 until REGION_SIZE && localY in 0 until REGION_SIZE && lowered[plane][localX][localY]

    companion object {
        private const val MAP_PLANES = 4
        private const val REGION_SIZE = 64

        fun from(grid: DecodedMapTileGrid): CollisionPlaneResolver {
            val lowered = Array(MAP_PLANES) { Array(REGION_SIZE) { BooleanArray(REGION_SIZE) } }
            for (plane in 0 until MAP_PLANES) {
                for (x in 0 until REGION_SIZE) {
                    for (y in 0 until REGION_SIZE) {
                        lowered[plane][x][y] = grid.getTile(x, y, plane).isBridge()
                    }
                }
            }
            return CollisionPlaneResolver(lowered)
        }
    }
}
