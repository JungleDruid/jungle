package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import ktx.math.vec2
import ktx.math.vec3
import net.natruid.jungle.utils.Layer

class TransformComponent(
        var position: Vector3 = vec3(),
        var scale: Vector2 = vec2(1f, 1f),
        var pivot: Vector2 = vec2(.5f, .5f),
        var rotation: Float = 0f,
        var visible: Boolean = true,
        var layer: Layer = Layer.DEFAULT
) : Component