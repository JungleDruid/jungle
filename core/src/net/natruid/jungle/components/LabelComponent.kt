package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align

class LabelComponent(
        var text: String = "",
        var fontName: String = "normal",
        var color: Color = Color.WHITE!!,
        var width: Float = 0f,
        var align: Int = Align.topLeft
) : Component