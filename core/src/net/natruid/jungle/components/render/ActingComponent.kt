package net.natruid.jungle.components.render

import com.artemis.PooledComponent

class ActingComponent : PooledComponent() {
    var actions = 0

    override fun reset() {
        actions = 0
    }
}
