package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram

class ShaderComponent(
        var shader: ShaderProgram = defaultShader,
        var blendSrcFunc: Int = GL20.GL_SRC_ALPHA,
        var blendDstFunc: Int = GL20.GL_ONE_MINUS_SRC_ALPHA
) : Component {
    companion object {
        val defaultShader = SpriteBatch.createDefaultShader()!!
    }
}