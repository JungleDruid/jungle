package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import net.natruid.jungle.utils.Point

class TileComponent(
        var coord: Point = Point(),
        var walkable: Boolean = true,
        var terrainType: TerrainType = TerrainType.NONE
) : Component {
    enum class TerrainType(val value: Byte) {
        NONE(0), DIRT(1), GRASS(2), WATER(3), ROAD(8);

        companion object {
            private val map = TerrainType.values().associateBy(TerrainType::value)
            fun fromByte(type: Byte) = map[type]
        }
    }

    override fun toString(): String {
        return coord.toString()
    }
}