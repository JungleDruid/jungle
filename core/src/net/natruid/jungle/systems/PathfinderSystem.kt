package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.badlogic.gdx.utils.BinaryHeap
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.utils.PathNode
import net.natruid.jungle.utils.Point
import net.natruid.jungle.utils.TerrainType
import java.util.*
import kotlin.collections.set

class PathfinderSystem : BaseSystem() {
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var sTile: TileSystem
    private val frontier = BinaryHeap<PathNode>()
    private val visited = HashMap<Int, PathNode>()
    private val walkables = ArrayList<Boolean>(4)
    private val walkableDiagonals = LinkedList<Boolean>()
    private val searchQueue = LinkedList<Int>()
    private val searchDirection = Point()

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
        goal: Int? = null,
        buildingRoad: Boolean = false
    ): Boolean {
        if (!diagonal) {
            walkables.clear()
            walkableDiagonals.clear()
        }
        if (diagonal && walkableDiagonals.size == 0) return false

        var costMultiplier = 1f

        searchQueue.clear()
        mTile[current.tile].let {
            if (buildingRoad && it.terrainType == TerrainType.WATER && current.prev != null) {
                searchDirection.set(it.coord)
                searchDirection *= 2
                searchDirection -= mTile[current.prev!!.tile].coord
                val next = sTile[searchDirection]
                if (next >= 0) searchQueue.add(next)
            } else {
                var roadCount = 0
                for (next in sTile.neighbors(it.coord, diagonal)) {
                    searchQueue.add(next)
                    if (buildingRoad) {
                        if (next >= 0) {
                            if (mTile[next].terrainType == TerrainType.WATER && it.terrainType != TerrainType.WATER)
                                costMultiplier += 1f
                            if (mTile[next].hasRoad) roadCount += 1
                        } else {
                            costMultiplier += 0.75f
                        }
                    }
                }
                if (buildingRoad && !it.hasRoad && roadCount > 1) return false
            }
        }

        while (searchQueue.isNotEmpty()) {
            val next = searchQueue.removeFirst()
            val nextTileComponent = if (next >= 0) mTile[next] else null
            val nextWalkable = nextTileComponent != null && nextTileComponent.obstacle < 0
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
                TerrainType.WATER -> if (!nextTileComponent.hasRoad) cost *= if (buildingRoad) 10f else 3f
                else -> {
                    if (nextTileComponent != null && nextTileComponent.hasRoad)
                        cost *= if (buildingRoad) 0.1f else 0.5f
                }
            }
            val nextCost = current.cost + cost * costMultiplier
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
        maxCost: Float?,
        diagonal: Boolean = true,
        buildingRoad: Boolean = false
    ): Array<PathNode> {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.pop()
            searchNeighbors(current, maxCost = maxCost, buildingRoad = buildingRoad)
            if (diagonal) searchNeighbors(current, true, maxCost)
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
            if (searchNeighbors(current, goal = goal, buildingRoad = false))
                return extractPath(visited.values, mTile[goal].coord)!!
            if (diagonal && searchNeighbors(current, true, goal = goal, buildingRoad = false))
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
