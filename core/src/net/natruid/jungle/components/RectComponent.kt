package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class RectComponent : Component {
    var width = 0f
    var height = 0f
    var color = Color.WHITE!!
    var type = ShapeRenderer.ShapeType.Line
}