package net.natruid.jungle.utils

import com.badlogic.gdx.utils.BinaryHeap
import com.badlogic.gdx.utils.Queue
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.systems.TileSystem
import kotlin.collections.set

class Pathfinder(private val tiles: TileSystem) {
    companion object {
        fun area(tiles: TileSystem, from: TileComponent, maxCost: Float): Collection<PathNode> {
            return Pathfinder(tiles).area(from, maxCost)
        }

        fun path(tiles: TileSystem, from: TileComponent, goal: TileComponent): Array<TileComponent>? {
            return Pathfinder(tiles).path(from, goal)
        }

        fun extractPath(pathNodes: Collection<PathNode>, goal: Point): Array<TileComponent>? {
            for (node in pathNodes) {
                if (goal == node.tile.coord) {
                    val path = ArrayList<TileComponent>()
                    var prevNode: PathNode? = node
                    while (prevNode != null) {
                        path.add(prevNode.tile)
                        prevNode = prevNode.prev
                    }
                    path.reverse()
                    return path.toTypedArray()
                }
            }
            return null
        }
    }

    private val frontier = BinaryHeap<PathNode>()
    private val visited = HashMap<TileComponent, PathNode>()

    private fun init(from: TileComponent): PathNode {
        val node = PathNode(from, 0f)
        frontier.add(node)
        visited[from] = node
        return node
    }

    private fun searchNeighbors(
        current: PathNode,
        diagonal: Boolean = false,
        maxCost: Float? = null,
        goal: TileComponent? = null
    ): Boolean {
        if (!diagonal) {
            walkables.clear()
            walkableDiagonals.clear()
        }
        if (diagonal && walkableDiagonals.size == 0) return false
        for (next in tiles.neighbors(current.tile.coord, diagonal)) {
            if (!diagonal) {
                val size = walkables.size
                if (size > 0) {
                    walkableDiagonals.addLast(next != null && next.walkable || walkables[size - 1])
                    if (size == 3) {
                        walkableDiagonals.addLast(next != null && next.walkable || walkables[0])
                    }
                }
                walkables.add(next != null && next.walkable)
            }
            var cost = if (!diagonal) 1f else 1.5f
            when (next?.terrainType) {
                TileComponent.TerrainType.WATER -> cost *= 3
                TileComponent.TerrainType.ROAD -> cost /= 2
                else -> {
                }
            }
            val nextCost = current.cost + cost
            if (
                diagonal && !walkableDiagonals.removeFirst()
                || next == null || !next.walkable
                || maxCost != null && nextCost > maxCost
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
                    priority += heuristic(goal.coord, next.coord, if (diagonal) .5f else 0f)
                }
                frontier.add(node, priority)
            }
        }
        return false
    }

    private val walkables = ArrayList<Boolean>(4)
    private val walkableDiagonals = Queue<Boolean>(4)
    fun area(
        from: TileComponent,
        maxCost: Float,
        diagonal: Boolean = true
    ): Collection<PathNode> {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.pop()
            searchNeighbors(current, maxCost = maxCost)
            if (diagonal) searchNeighbors(current, true, maxCost = maxCost)
        }
        return visited.values
    }

    private fun heuristic(a: Point, b: Point, f: Float): Float {
        return (Math.abs(a.x - b.x) + Math.abs(a.y - b.y)).toFloat() + f
    }

    fun path(
        from: TileComponent,
        goal: TileComponent,
        diagonal: Boolean = true
    ): Array<TileComponent>? {
        init(from)
        while (!frontier.isEmpty) {
            val current = frontier.pop()
            if (searchNeighbors(current, goal = goal)) return extractPath(visited.values, goal.coord)!!
            if (diagonal && searchNeighbors(current, true, goal = goal)) return extractPath(visited.values, goal.coord)!!
        }
        return null
    }
}
