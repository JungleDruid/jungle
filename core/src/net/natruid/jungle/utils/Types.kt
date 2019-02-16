package net.natruid.jungle.utils

enum class TerrainType(val value: Byte) {
    NONE(0), DIRT(1), GRASS(2), WATER(16);

    companion object {
        private val map = TerrainType.values().associateBy(TerrainType::value)
        fun fromByte(type: Byte) = map[type]
    }
}

enum class IndicatorType { MOVE_AREA, MOVE_PATH }

enum class ObstacleType { TREE, ROCK, WALL }

enum class RendererType { NONE, SPRITE_BATCH, SHAPE_RENDERER }

enum class ShaderUniformType(val value: Int) {
    UNKNOWN(0),
    UNIFORM1F(1), UNIFORM2F(2), UNIFORM3F(3), UNIFORM4F(4),
    UNIFORM1FV(5), UNIFORM2FV(6), UNIFORM3FV(7), UNIFORM4FV(8),
    UNIFORM1I(9), UNIFORM2I(10), UNIFORM3I(11), UNIFORM4I(12)
}
