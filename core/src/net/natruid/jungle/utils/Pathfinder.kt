package net.natruid.jungle.utils

import com.badlogic.gdx.utils.Queue
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.systems.TileSystem
import kotlin.collections.set

class Pathfinder {
    companion object {
        fun area(tiles: TileSystem, from: TileComponent, length: Float): ArrayList<PathNode> {
            val pf = Pathfinder()
            pf.area(tiles, from, length)
            return pf.result
        }
    }

    private val frontier = Queue<TileComponent>()
    private val visited = HashMap<TileComponent, PathNode>()
    private var locked = false
    val result: ArrayList<PathNode>
        get() {
            if (locked) {
                error("[Error] Pathfinder - Cannot obtain the result more than once.")
            }
            locked = true
            val ret = ArrayList<PathNode>()
            for (node in visited.values) {
                ret.add(node)
            }
            return ret
        }

    private fun init(from: TileComponent) {
        frontier.addLast(from)
        visited[from] = PathNode(from, 0f)
    }

    private val walkables = ArrayList<Boolean>(4)
    private val walkableDiagonals = Queue<Boolean>(4)
    private fun area(tiles: TileSystem, from: TileComponent, length: Float, diagonal: Boolean = true): Pathfinder {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.removeFirst()
            val node = visited[current]!!
            for (next in tiles.neighbors(current.coord)) {
                if (diagonal) {
                    val size = walkables.size
                    if (size > 0) {
                        walkableDiagonals.addLast(next != null && next.walkable || walkables[size - 1])
                        if (size == 3) {
                            walkableDiagonals.addLast(next != null && next.walkable || walkables[0])
                        }
                    }
                    walkables.add(next != null && next.walkable)
                }
                val nextLength = node.length + 1
                if (next == null || !next.walkable || nextLength > length) continue
                val v = visited[next]
                if (v == null || v.length > nextLength) {
                    frontier.addLast(next)
                    if (v == null) {
                        visited[next] = PathNode(next, nextLength, node)
                    } else {
                        v.length = nextLength
                        v.prev = node
                    }
                }
            }
            if (diagonal) {
                if (walkableDiagonals.size > 0) {
                    for (next in tiles.neighbors(current.coord, true)) {
                        if (walkableDiagonals.size == 0) break
                        val nextLength = node.length + 1.5f
                        if (!walkableDiagonals.removeFirst() || next == null || !next.walkable || nextLength > length) {
                            continue
                        }
                        val v = visited[next]
                        if (v == null || v.length > nextLength) {
                            frontier.addLast(next)
                            if (v == null) {
                                visited[next] = PathNode(next, nextLength, node)
                            } else {
                                v.length = nextLength
                                v.prev = node
                            }
                        }
                    }
                }
                walkables.clear()
                walkableDiagonals.clear()
            }
        }
        return this
    }
}