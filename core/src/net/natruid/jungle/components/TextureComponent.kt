package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool

class TextureComponent : Component, Pool.Poolable {
    var region: TextureRegion? = null
        set (texture) {
            field = texture
            field?.texture?.setFilter(Linear, Linear)
        }

    override fun reset() {
        region = null
    }
}