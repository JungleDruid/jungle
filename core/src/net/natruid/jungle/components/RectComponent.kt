package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class RectComponent(
    var width: Float = 0f,
    var height: Float = 0f,
    var color: Color = Color.WHITE!!,
    var type: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Line
) : Component
