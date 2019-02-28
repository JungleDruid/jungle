package net.natruid.jungle.components.render

import com.artemis.PooledComponent

class AngleComponent : PooledComponent() {
    companion object {
        val NONE = AngleComponent()
    }

    var rotation = 0f

    override fun reset() {
        rotation = 0f
    }
}
