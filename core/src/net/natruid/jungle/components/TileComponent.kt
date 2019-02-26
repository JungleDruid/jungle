package net.natruid.jungle.components

import com.artemis.Component
import com.artemis.annotations.EntityId
import net.natruid.jungle.utils.Point
import net.natruid.jungle.utils.TerrainType

class TileComponent : Component() {
    val coord: Point = Point()
    var terrainType: TerrainType = TerrainType.NONE
    var hasRoad: Boolean = false
    @EntityId
    var unit: Int = -1
    @EntityId
    var obstacle: Int = -1

    override fun toString(): String {
        return coord.toString()
    }
}
