package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class TileComponent : Component, Pool.Poolable {
    var x = 0
    var y = 0
    var walkable = true

    override fun reset() {
        x = 0
        y = 0
        walkable = true
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}