package net.natruid.jungle.utils

data class Point(var x: Int, var y: Int) {
    constructor() : this(0, 0)

    fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun set(target: Point) {
        this.x = target.x
        this.y = target.y
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

    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun times(other: Point): Point {
        return Point(x * other.x, y * other.y)
    }

    operator fun div(other: Point): Point {
        return Point(x / other.x, y / other.y)
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

    operator fun plus(other: ImmutablePoint): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: ImmutablePoint): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun times(other: ImmutablePoint): Point {
        return Point(x * other.x, y * other.y)
    }

    operator fun div(other: ImmutablePoint): Point {
        return Point(x / other.x, y / other.y)
    }
}
