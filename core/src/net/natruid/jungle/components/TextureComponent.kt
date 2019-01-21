package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture.TextureFilter.*
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TextureComponent : Component {
    var region: TextureRegion? = null
        set (texture) {
            field = texture
            field?.texture?.setFilter(Linear, Linear)
        }
}