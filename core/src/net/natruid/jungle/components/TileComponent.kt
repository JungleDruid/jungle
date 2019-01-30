package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import net.natruid.jungle.utils.Point

class TileComponent(var coord: Point = Point(), var walkable: Boolean = true) : Component {
    override fun toString(): String {
        return coord.toString()
    }
}