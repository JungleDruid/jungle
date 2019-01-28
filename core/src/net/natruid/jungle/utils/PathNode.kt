package net.natruid.jungle.utils

import com.badlogic.gdx.utils.Pool
import net.natruid.jungle.components.TileComponent

data class PathNode(var tile: TileComponent?, var length: Float) : Pool.Poolable {
    companion object {
        val pool = object : Pool<PathNode>() {
            override fun newObject(): PathNode {
                return PathNode(null, 0f)
            }
        }

        fun obtain(tile: TileComponent, length: Float): PathNode {
            val ret = pool.obtain()
            ret.tile = tile
            ret.length = length
            return ret
        }
    }

    override fun reset() {
        tile = null
        length = 0f
    }

    fun free() {
        pool.free(this)
    }
}