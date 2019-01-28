package net.natruid.jungle.utils

import com.badlogic.gdx.utils.Pool

class ImmutablePoint(private var point: Point?) : Pool.Poolable {
    companion object {
        private val pool by lazy {
            object : Pool<ImmutablePoint>() {
                override fun newObject(): ImmutablePoint {
                    return ImmutablePoint(null)
                }
            }
        }

        fun obtain(point: Point): ImmutablePoint {
            val ret = pool.obtain()
            ret.point = point
            return ret
        }
    }

    val x get() = point!!.x
    val y get() = point!!.y

    override fun reset() {
        point = null
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}