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

        fun freeResult(result: GdxArray<PathNode>) {
            PathNode.pool.freeAll(result)
            result.clear()
            resultPool.free(result)
        }
    }

    private val frontier = Queue<TileComponent>()
    private val visited = ObjectMap<TileComponent, Float>()
    val result: GdxArray<PathNode>
        get() {
            val ret = resultPool.obtain()
            for (entry in visited) {
                ret.add(PathNode.obtain(entry.key, entry.value))
            }
            return ret
        }

    private fun init(from: TileComponent) {
        frontier.addLast(from)
        visited[from] = 0f
    }

    private val walkables = GdxArray<Boolean>(4)
    private val walkableDiagonals = Queue<Boolean>(4)
    private fun area(tiles: TileSystem, from: TileComponent, length: Float, diagonal: Boolean = true): Pathfinder {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.removeFirst()
            val steps = visited[current]
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
                if (!next.walkable || steps + 1 > length) continue
                val v = visited.get(next)
                if (v == null || v > steps + 1) {
                    frontier.addLast(next)
                    visited[next] = steps + 1
                }
            }
            if (diagonal) {
                if (walkableDiagonals.size > 0) {
                    for (next in tiles.neighbors(current.x, current.y, true)) {
                        if (walkableDiagonals.size == 0) break
                        if (!walkableDiagonals.removeFirst() || !next.walkable || steps + 1.5f > length) continue
                        val v = visited.get(next)
                        if (v == null || v > steps + 1.5f) {
                            frontier.addLast(next)
                            visited[next] = steps + 1.5f
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
        visited.clear()
    }

    fun free() {
        pool.free(this)
    }
}