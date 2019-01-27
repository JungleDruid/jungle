package net.natruid.jungle.utils

data class Point(var x: Int, var y: Int) {

    constructor(hasValue: Boolean) : this(if (hasValue) 0 else Int.MIN_VALUE, if (hasValue) 0 else Int.MIN_VALUE)

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
}