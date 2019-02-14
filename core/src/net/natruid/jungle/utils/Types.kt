package net.natruid.jungle.utils

enum class TerrainType(val value: Byte) {
    NONE(0), DIRT(1), GRASS(2), ROAD(8), BRIDGE(9), WATER(16);

    companion object {
        private val map = TerrainType.values().associateBy(TerrainType::value)
        fun fromByte(type: Byte) = map[type]
    }
}

enum class IndicatorType { MOVE_AREA, MOVE_PATH }

enum class ObstacleType { TREE, ROCK, WALL }

enum class RendererType { NONE, SPRITE_BATCH, SHAPE_RENDERER }
