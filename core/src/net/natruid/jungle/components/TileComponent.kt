package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class TileComponent : Component, Pool.Poolable {
    var walkable = true

    override fun reset() {
        walkable = true
    }
}