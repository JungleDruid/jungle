package net.natruid.jungle.utils

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import ktx.collections.GdxArray
import ktx.collections.set
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.systems.TileSystem

class Pathfinder : Pool.Poolable {
    companion object {
        private val pool = object : Pool<Pathfinder>() {
            override fun newObject(): Pathfinder {
                return Pathfinder()
            }
        }

        private val resultPool = object : Pool<GdxArray<PathNode>>() {
            override fun newObject(): GdxArray<PathNode> {
                return GdxArray()
            }
        }

        fun area(tiles: TileSystem, from: TileComponent, length: Float): GdxArray<PathNode> {
            val pf = pool.obtain()
            pf.area(tiles, from, length)
            val ret = pf.result
            pf.free()
            return ret
        }

        fun freeResult(result: GdxArray<PathNode>?) {
            if (result == null) return
            PathNode.pool.freeAll(result)
            result.clear()
            resultPool.free(result)
        }
    }

    private val frontier = Queue<TileComponent>()
    private val visited = ObjectMap<TileComponent, PathNode>()
    private var locked = false
    val result: GdxArray<PathNode>
        get() {
            if (locked) {
                error("[Error] Pathfinder - Cannot obtain the result more than once.")
            }
            locked = true
            val ret = resultPool.obtain()
            for (node in visited.values()) {
                ret.add(node)
            }
            return ret
        }

    private fun init(from: TileComponent) {
        frontier.addLast(from)
        visited[from] = PathNode.obtain(from, 0f)
    }

    private val walkables = GdxArray<Boolean>(4)
    private val walkableDiagonals = Queue<Boolean>(4)
    private fun area(tiles: TileSystem, from: TileComponent, length: Float, diagonal: Boolean = true): Pathfinder {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.removeFirst()
            val node = visited[current]
            for (next in tiles.neighbors(current.x, current.y)) {
                if (diagonal) {
                    val size = walkables.size
                    if (size > 0) {
                        walkableDiagonals.addLast(next.walkable || walkables[size - 1])
                        if (size == 3) {
                            walkableDiagonals.addLast(next.walkable || walkables[0])
                        }
                    }
                    walkables.add(next.walkable)
                }
                val nextLength = node.length + 1
                if (!next.walkable || nextLength > length) continue
                val v = visited.get(next)
                if (v == null || v.length > nextLength) {
                    frontier.addLast(next)
                    if (v == null) {
                        visited[next] = PathNode.obtain(next, nextLength, node)
                    } else {
                        v.length = nextLength
                        v.prev = node
                    }
                }
            }
            if (diagonal) {
                if (walkableDiagonals.size > 0) {
                    for (next in tiles.neighbors(current.x, current.y, true)) {
                        if (walkableDiagonals.size == 0) break
                        val nextLength = node.length + 1.5f
                        if (!walkableDiagonals.removeFirst() || !next.walkable || nextLength > length) continue
                        val v = visited.get(next)
                        if (v == null || v.length > nextLength) {
                            frontier.addLast(next)
                            if (v == null) {
                                visited[next] = PathNode.obtain(next, nextLength, node)
                            } else {
                                v.length = nextLength
                                v.prev = node
                            }
                        }
                    }
                }
                walkables.clear()
            }
        }
        return this
    }

    override fun reset() {
        frontier.clear()
        if (!locked) {
            for (node in visited.values()) {
                node.free()
            }
        }
        visited.clear()
        locked = false
    }

    fun free() {
        pool.free(this)
    }
}