package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.badlogic.gdx.utils.BinaryHeap
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.utils.PathNode
import net.natruid.jungle.utils.Point
import java.util.*
import kotlin.collections.set

class PathfinderSystem : BaseSystem() {
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var sTile: TileSystem
    private val frontier = BinaryHeap<PathNode>()
    private val visited = HashMap<Int, PathNode>()
    private val walkables = ArrayList<Boolean>(4)
    private val walkableDiagonals = LinkedList<Boolean>()

    private fun init(from: Int): PathNode {
        val node = PathNode(from, 0f)
        frontier.clear()
        visited.clear()
        frontier.add(node)
        visited[from] = node
        return node
    }

    private fun searchNeighbors(
        current: PathNode,
        diagonal: Boolean = false,
        maxCost: Float? = null,
        goal: Int? = null
    ): Boolean {
        if (!diagonal) {
            walkables.clear()
            walkableDiagonals.clear()
        }
        if (diagonal && walkableDiagonals.size == 0) return false
        for (next in sTile.neighbors(mTile[current.tile].coord, diagonal)) {
            val nextTileComponent = if (next >= 0) mTile[next] else null
            val nextWalkable = nextTileComponent != null && nextTileComponent.walkable
            if (!diagonal) {
                val size = walkables.size
                if (size > 0) {
                    walkableDiagonals.addLast(nextWalkable || walkables[size - 1])
                    if (size == 3) {
                        walkableDiagonals.addLast(nextWalkable || walkables[0])
                    }
                }
                walkables.add(nextWalkable)
            }
            var cost = if (!diagonal) 1f else 1.5f
            when (nextTileComponent?.terrainType) {
                TileComponent.TerrainType.WATER -> cost *= 3
                TileComponent.TerrainType.ROAD -> cost /= 2
                else -> {
                }
            }
            val nextCost = current.cost + cost
            if (
                diagonal && !walkableDiagonals.removeFirst()
                || !nextWalkable || maxCost != null && nextCost > maxCost
            ) continue
            val nextNode = this.visited[next]
            if (nextNode == null || nextNode.cost > nextCost) {
                val node = nextNode ?: PathNode(next, nextCost, current)
                if (nextNode == null) {
                    this.visited[next] = node
                    if (node.tile == goal) return true
                } else {
                    nextNode.cost = nextCost
                    nextNode.prev = current
                }
                var priority = nextCost
                if (goal != null) {
                    priority += heuristic(
                        mTile[goal].coord,
                        nextTileComponent!!.coord,
                        if (diagonal) .5f else 0f
                    )
                }
                frontier.add(node, priority)
            }
        }
        return false
    }

    fun area(
        from: Int,
        maxCost: Float,
        diagonal: Boolean = true
    ): Array<PathNode> {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.pop()
            searchNeighbors(current, maxCost = maxCost)
            if (diagonal) searchNeighbors(current, true, maxCost = maxCost)
        }
        return visited.values.toTypedArray()
    }

    private fun heuristic(a: Point, b: Point, f: Float): Float {
        return (Math.abs(a.x - b.x) + Math.abs(a.y - b.y)).toFloat() + f
    }

    fun path(
        from: Int,
        goal: Int,
        diagonal: Boolean = true
    ): Deque<Int>? {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.pop()
            if (searchNeighbors(current, goal = goal))
                return extractPath(visited.values, mTile[goal].coord)!!
            if (diagonal && searchNeighbors(current, true, goal = goal))
                return extractPath(visited.values, mTile[goal].coord)!!
        }
        return null
    }

    fun extractPath(pathNodes: Iterable<PathNode>, goal: Point): Deque<Int>? {
        for (node in pathNodes) {
            if (goal == mTile[node.tile].coord) {
                val path = LinkedList<Int>()
                var prevNode: PathNode? = node
                while (prevNode != null) {
                    path.addFirst(prevNode.tile)
                    prevNode = prevNode.prev
                }
                return path
            }
        }
        return null
    }

    override fun processSystem() {}
}
