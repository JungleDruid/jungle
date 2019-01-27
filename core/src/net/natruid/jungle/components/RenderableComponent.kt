package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class RenderableComponent : Component, Pool.Poolable {
    var renderCallback: ((TransformComponent) -> Unit)? = null
    fun render(transform: TransformComponent) {
        renderCallback?.invoke(transform)
    }

    override fun reset() {
        renderCallback = null
    }
}