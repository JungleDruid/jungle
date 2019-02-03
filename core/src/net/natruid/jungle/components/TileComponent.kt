package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import net.natruid.jungle.utils.Point

class TileComponent(
        var coord: Point = Point(),
        var walkable: Boolean = true,
        var terrainType: TerrainType = TerrainType.NONE
) : Component {
    enum class TerrainType(val value: Byte) { NONE(0), DIRT(1), GRASS(2), ROAD(8) }

    override fun toString(): String {
        return coord.toString()
    }
}