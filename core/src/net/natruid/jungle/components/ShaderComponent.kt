package net.natruid.jungle.components

import com.artemis.Component
import com.badlogic.gdx.graphics.GL20
import net.natruid.jungle.utils.Shader

class ShaderComponent(
    var shader: Shader = Shader(),
    var blendSrcFunc: Int = GL20.GL_SRC_ALPHA,
    var blendDstFunc: Int = GL20.GL_ONE_MINUS_SRC_ALPHA
) : Component()
