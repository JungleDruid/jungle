package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

class CircleComponent : Component, Pool.Poolable {
    var radius = 0f
    val color = Color(Color.WHITE)
    var type = ShapeRenderer.ShapeType.Line

    override fun reset() {
        radius = 0f
        Pools.free(color)
        color.set(Color.WHITE)
        type = ShapeRenderer.ShapeType.Line
    }
}
