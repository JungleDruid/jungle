package net.natruid.jungle.components.render

import com.artemis.PooledComponent
import com.badlogic.gdx.math.Vector2
import ktx.math.divAssign
import ktx.math.timesAssign

abstract class Vector2Component(x: Float = 0f, y: Float = 0f) : PooledComponent() {

    val xy: Vector2 = Vector2(x, y)

    var x
        get() = xy.x
        set(value) {
            xy.x = value
        }

    var y
        get() = xy.y
        set(value) {
            xy.y = value
        }

    fun set(x: Float, y: Float) {
        xy.set(x, y)
    }

    fun set(other: Vector2Component) {
        xy.set(other.xy)
    }

    fun set(other: Vector2) {
        xy.set(other)
    }

    operator fun plusAssign(other: Vector2) {
        xy.add(other)
    }

    operator fun minusAssign(other: Vector2) {
        xy.sub(other)
    }

    operator fun timesAssign(other: Vector2) {
        xy.timesAssign(other)
    }

    operator fun timesAssign(scalar: Float) {
        xy.timesAssign(scalar)
    }

    operator fun timesAssign(scalar: Int) {
        xy.timesAssign(scalar)
    }

    operator fun divAssign(other: Vector2) {
        xy.divAssign(other)
    }

    operator fun divAssign(scalar: Float) {
        xy.divAssign(scalar)
    }

    operator fun divAssign(scalar: Int) {
        xy.divAssign(scalar)
    }

    override fun reset() {
        xy.set(Vector2.Zero)
    }
}
