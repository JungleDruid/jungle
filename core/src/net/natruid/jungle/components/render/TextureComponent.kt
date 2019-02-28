package net.natruid.jungle.components.render

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TextureComponent : Component() {
    var region: TextureRegion? = null
        set (texture) {
            field = texture
            field?.texture?.setFilter(Linear, Linear)
        }
    var flipX: Boolean = false
    var flipY: Boolean = false
    val color: Color = Color(Color.WHITE)
}
