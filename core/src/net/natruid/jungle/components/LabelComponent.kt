package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align

class LabelComponent : Component {
    var text = ""
    var fontName = "normal"
    var color = Color.WHITE!!
    var width = 0f
    var align = Align.topLeft
}