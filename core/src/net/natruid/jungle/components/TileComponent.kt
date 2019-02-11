package net.natruid.jungle.components

import com.artemis.Component
import com.artemis.annotations.EntityId
import net.natruid.jungle.utils.Point

class TileComponent(
    var coord: Point = Point(),
    var walkable: Boolean = true,
    var terrainType: TerrainType = TerrainType.NONE,
    @EntityId var unit: Int = -1
) : Component() {

    enum class TerrainType(val value: Byte) {
        NONE(0), DIRT(1), GRASS(2), ROAD(8), BRIDGE(9), WATER(16);

        companion object {
            private val map = TerrainType.values().associateBy(TerrainType::value)
            fun fromByte(type: Byte) = map[type]
        }
    }

    override fun toString(): String {
        return coord.toString()
    }
}
