package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class UnitComponent : Component, Pool.Poolable {
    var x = 0
    var y = 0

    override fun reset() {
        x = 0
        y = 0
    }
}