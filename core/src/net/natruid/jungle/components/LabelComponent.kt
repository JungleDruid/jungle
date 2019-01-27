package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Pool

class LabelComponent : Component, Pool.Poolable {
    var text = ""
    var fontName = "normal"
    val color = Color(Color.WHITE)
    var width = 0f
    var align = Align.topLeft

    override fun reset() {
        text = ""
        fontName = "normal"
        color.set(Color.WHITE)
        width = 0f
        align = Align.topLeft
    }
}