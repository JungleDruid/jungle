package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

class RectComponent : Component, Pool.Poolable {
    var width = 0f
    var height = 0f
    val color = Color(Color.WHITE)
    var type = ShapeRenderer.ShapeType.Line

    override fun reset() {
        width = 0f
        height = 0f
        Pools.free(color)
        color.set(Color.WHITE)
        type = ShapeRenderer.ShapeType.Line
    }
}