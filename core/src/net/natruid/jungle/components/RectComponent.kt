package net.natruid.jungle.components

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class RectComponent : Component() {
    var width: Float = 0f
    var height: Float = 0f
    val color: Color = Color(Color.WHITE)
    var type: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Line
}
