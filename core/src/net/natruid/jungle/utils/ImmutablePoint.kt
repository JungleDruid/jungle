package net.natruid.jungle.utils

class ImmutablePoint(private var point: Point?) {
    val x get() = point!!.x
    val y get() = point!!.y

    override fun toString(): String {
        return "($x, $y)"
    }
}