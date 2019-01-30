package net.natruid.jungle.components

import com.badlogic.ashley.core.Component

class RenderableComponent(var renderCallback: ((TransformComponent) -> Unit)? = null) : Component {
    fun render(transform: TransformComponent) {
        renderCallback?.invoke(transform)
    }
}