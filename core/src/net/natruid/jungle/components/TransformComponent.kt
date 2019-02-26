package net.natruid.jungle.components

import com.artemis.Component
import com.badlogic.gdx.math.Vector2
import net.natruid.jungle.utils.Layer

class TransformComponent : Component() {
    val position: Vector2 = Vector2()
    var z: Float = 0f
    val scale: Vector2 = Vector2(1f, 1f)
    val pivot: Vector2 = Vector2(.5f, .5f)
    var rotation: Float = 0f
    var visible: Boolean = true
    var layer: Layer = Layer.DEFAULT
}
