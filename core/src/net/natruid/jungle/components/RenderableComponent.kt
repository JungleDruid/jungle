package net.natruid.jungle.components

import com.artemis.Component

class RenderableComponent : Component() {
    var renderCallback: ((TransformComponent) -> Unit)? = null
    fun render(transform: TransformComponent) {
        renderCallback?.invoke(transform)
    }
}
