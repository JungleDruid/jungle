package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TextureComponent : Component {
    var region: TextureRegion? = null
        set (texture) {
            field = texture
            field?.texture?.setFilter(Linear, Linear)
        }
    var flipX: Boolean = false
    var flipY: Boolean = false
    var color: Color = Color.WHITE
}