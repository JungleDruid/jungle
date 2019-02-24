package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.badlogic.gdx.utils.BinaryHeap
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.utils.*
import java.util.*
import kotlin.collections.set

class PathfinderSystem : BaseSystem() {
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var tileSystem: TileSystem
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
                val next = tileSystem[searchDirection]
                if (next >= 0) searchQueue.add(next)
            } else {
                var roadCount = 0
                for (next in tileSystem.neighbors(it.coord, diagonal)) {
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
    ): Area {
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
    ): Path? {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.pop()
            if (searchNeighbors(current, goal = goal, buildingRoad = false))
                return extractPath(visited.values, goal)!!
            if (diagonal && searchNeighbors(current, true, goal = goal, buildingRoad = false))
                return extractPath(visited.values, goal)!!
        }
        return null
    }

    fun extractPath(
        area: Area,
        goal: Int,
        unit: Int = -1,
        type: ExtractPathType = ExtractPathType.EXACT,
        maxCost: Float = Float.NaN,
        inRange: Float = Float.NaN
    ): Path? {
        return extractPath(area.asIterable(), goal, unit, type, maxCost, inRange)
    }

    private fun extractPath(
        pathNodes: Iterable<PathNode>,
        goal: Int,
        unit: Int = -1,
        type: ExtractPathType = ExtractPathType.EXACT,
        maxCost: Float = Float.NaN,
        inRange: Float = Float.NaN
    ): Path? {
        if (type == ExtractPathType.EXACT || type == ExtractPathType.CLOSEST) {
            if (unit < 0 || mTile[goal].unit < 0) {
                for (node in pathNodes) {
                    if (goal == node.tile) {
                        return buildPath(node, unit, maxCost)
                    }
                }
            }
            if (type == ExtractPathType.EXACT) return null
        }

        var bestNode: PathNode? = null
        var minDist = Float.MAX_VALUE
        for (node in pathNodes) {
            val unit1 = mTile[node.tile].unit
            if (unit > 0 && unit1 >= 0 && unit != unit1) continue
            val dist = tileSystem.getDistance(node.tile, goal)
            if (!inRange.isNaN() && dist > inRange) continue
            val better = if (bestNode == null) true else when (type) {
                ExtractPathType.CLOSEST -> dist < minDist || dist == minDist && node.cost < bestNode.cost
                ExtractPathType.LOWEST_COST -> node.cost < bestNode.cost
                else -> error("IMPOSSIBLE! $type")
            }
            if (better) {
                bestNode = node
                minDist = dist
            }
        }

        if (bestNode != null) return buildPath(bestNode, unit, maxCost)

        return null
    }

    private fun buildPath(node: PathNode, unit: Int = -1, maxCost: Float = Float.NaN): Path {
        val path = LinkedList<PathNode>()
        var current: PathNode? = node
        while (current != null) {
            val unit1 = mTile[current.tile].unit
            if ((maxCost.isNaN() || current.cost <= maxCost) && (unit < 0 || unit1 < 0 || unit == unit1)) {
                path.addFirst(current)
            }
            current = current.prev
        }
        return path
    }

    override fun processSystem() {}
}
