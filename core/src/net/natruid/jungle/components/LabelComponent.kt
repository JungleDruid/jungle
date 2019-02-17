package net.natruid.jungle.components

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align

class LabelComponent : Component() {
    var text: String = ""
    var fontName: String = "normal"
    var color: Color = Color.WHITE!!
    var width: Float = 0f
    var align: Int = Align.topLeft
}
