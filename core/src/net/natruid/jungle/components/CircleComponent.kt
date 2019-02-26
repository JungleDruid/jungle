package net.natruid.jungle.components

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class CircleComponent : Component() {
    var radius: Float = 0f
    val color: Color = Color(Color.WHITE)
    var type: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Line
}
