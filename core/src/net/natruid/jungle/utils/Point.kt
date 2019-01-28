package net.natruid.jungle.utils

import com.badlogic.gdx.utils.Pool

data class Point(var x: Int, var y: Int) : Pool.Poolable {
    companion object {
        private val pool by lazy {
            object : Pool<Point>() {
                override fun newObject(): Point {
                    return Point()
                }
            }
        }

        fun obtain(): Point {
            return pool.obtain()
        }

        fun obtain(x: Int, y: Int): Point {
            val ret = pool.obtain()
            ret.x = x
            ret.y = y
            return ret
        }
    }

    constructor() : this(Int.MIN_VALUE, Int.MIN_VALUE)

    fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun set(target: Point) {
        this.x = target.x
        this.y = target.y
    }

    val hasValue get() = this.x != Int.MIN_VALUE || this.y != Int.MIN_VALUE

    fun setToNone() {
        this.set(Int.MIN_VALUE, Int.MIN_VALUE)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Point) {
            return x == other.x && y == other.y
        }
        return false
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    operator fun plusAssign(other: Point) {
        this.x += other.x
        this.y += other.y
    }

    operator fun minusAssign(other: Point) {
        this.x -= other.x
        this.y -= other.y
    }

    operator fun timesAssign(other: Point) {
        this.x *= other.x
        this.y *= other.y
    }

    operator fun divAssign(other: Point) {
        this.x /= other.x
        this.y /= other.y
    }

    operator fun plusAssign(other: ImmutablePoint) {
        this.x += other.x
        this.y += other.y
    }

    operator fun minusAssign(other: ImmutablePoint) {
        this.x -= other.x
        this.y -= other.y
    }

    operator fun timesAssign(other: ImmutablePoint) {
        this.x *= other.x
        this.y *= other.y
    }

    operator fun divAssign(other: ImmutablePoint) {
        this.x /= other.x
        this.y /= other.y
    }

    override fun reset() {
        setToNone()
    }

    fun free() {
        pool.free(this)
    }
}