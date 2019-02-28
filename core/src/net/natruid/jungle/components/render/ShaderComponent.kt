package net.natruid.jungle.components.render

import com.artemis.Component
import com.badlogic.gdx.graphics.GL20
import net.natruid.jungle.utils.Shader

class ShaderComponent : Component() {
    companion object {
        val DEFAULT = ShaderComponent()
    }

    var shader: Shader = Shader.defaultShader
    var blendSrcFunc: Int = GL20.GL_SRC_ALPHA
    var blendDstFunc: Int = GL20.GL_ONE_MINUS_SRC_ALPHA
}
