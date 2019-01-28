package net.natruid.jungle.utils

import com.badlogic.gdx.utils.Pool
import net.natruid.jungle.components.TileComponent

data class PathNode(var tile: TileComponent?, var length: Float, var prev: PathNode?) : Pool.Poolable {
    companion object {
        val pool = object : Pool<PathNode>() {
            override fun newObject(): PathNode {
                return PathNode(null, 0f, null)
            }
        }

        fun obtain(tile: TileComponent?, length: Float, prev: PathNode? = null): PathNode {
            val ret = pool.obtain()
            ret.tile = tile
            ret.length = length
            ret.prev = prev
            return ret
        }
    }

    override fun reset() {
        tile = null
        length = 0f
        prev = null
    }

    fun free() {
        pool.free(this)
    }
}