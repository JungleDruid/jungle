package net.natruid.jungle.components.render

import com.artemis.PooledComponent

class RenderComponent : PooledComponent() {
    var z = 0f

    override fun reset() {
        z = 0f
    }
}
