package net.natruid.jungle.components

import com.artemis.Component

class RenderableComponent(var renderCallback: ((TransformComponent) -> Unit)? = null) : Component() {
    fun render(transform: TransformComponent) {
        renderCallback?.invoke(transform)
    }
}
