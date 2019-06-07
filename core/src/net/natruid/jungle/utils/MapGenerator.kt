package net.natruid.jungle.utils

import com.artemis.ArchetypeBuilder
import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.utils.IntBag
import com.badlogic.gdx.math.RandomXS128
import net.natruid.jungle.components.ObstacleComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.render.PosComponent
import net.natruid.jungle.components.render.RenderComponent
import net.natruid.jungle.components.render.TextureComponent
import net.natruid.jungle.systems.PathfinderSystem
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MapGenerator(
    private val columns: Int,
    private val rows: Int,
    private val world: World,
    private val seed: Long = Random.nextLong()
) {
    private val tileArchetype = ArchetypeBuilder().add(
        TileComponent::class.java,
        RenderComponent::class.java,
        PosComponent::class.java,
        TextureComponent::class.java
    ).build(world)
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var mObstacle: ComponentMapper<ObstacleComponent>
    private lateinit var pathfinderSystem: PathfinderSystem
    private lateinit var map: Array<IntArray>
    val random = RandomXS128(seed)
    private val emptyTiles = IntBag(columns * rows)
    private var initialized = false

    fun init(): Array<IntArray> {
        Logger.debug("Map seed: $seed")
        map = Array(columns) { x ->
            IntArray(rows) { y ->
                val entityId = world.create(tileArchetype)
                mTile[entityId].coord.set(x, y)
                emptyTiles.add(entityId)
                entityId
            }
        }
        initialized = true
        return map
    }

    private fun getTile(x: Int, y: Int, reversed: Boolean = false): Int {
        if (x < 0 || y < 0) return -1
        if (!reversed) {
            if (x >= columns || y >= rows) return -1
        } else {
            if (x >= rows || y >= columns) return -1
        }
        return if (!reversed) {
            map[x][y]
        } else {
            map[y][x]
        }
    }

    private fun createLine(
        terrainType: TerrainType = TerrainType.WATER,
        minWidth: Int = 1,
        maxWidth: Int = 3,
        vertical: Boolean = random.nextBoolean(),
        fork: Boolean = false,
        mutationFactor: Long = 30L
    ) {
        val ref = if (vertical) columns else rows
        val length = if (vertical) rows else columns
        val startRange = ref / 2
        var wMid = random.nextInt(startRange) + (ref - startRange) / 2
        var width = random.nextInt(maxWidth - minWidth) + minWidth
        var mutateChance = 0L
        var lRange: IntProgression = 0 until length
        if (random.nextBoolean()) lRange = lRange.reversed()
        for (l in lRange) {
            if (fork && mTile[getTile(l, wMid, vertical)].terrainType == terrainType) {
                break
            }
            var noMutation = false
            for (w in 0 until width) {
                val tile = getTile(l, wMid + w - width / 2, vertical)
                if (tile < 0) continue
                if (mTile[tile].terrainType == TerrainType.WATER) {
                    noMutation = true
                }
                replaceTile(tile, terrainType)
            }
            if (noMutation) continue
            if (mutateChance >= 100L || random.nextLong(100) >= 100L - mutateChance) {
                mutateChance = 0L
                if (random.nextBoolean()) {     // direction mutation
                    if (random.nextBoolean()) {
                        if (wMid < ref - 1) {
                            wMid += 1
                            replaceTile(
                                getTile(l, min(ref - 1, wMid + width / 2 - 1 + width.rem(2)), vertical),
                                terrainType
                            )
                        }
                    } else {
                        if (wMid > 0) {
                            wMid -= 1
                            replaceTile(
                                getTile(l, max(0, wMid - width / 2), vertical),
                                terrainType
                            )
                        }
                    }
                } else {                        // width mutation
                    width += when {
                        width == maxWidth -> -1
                        width == minWidth || random.nextBoolean() -> 1
                        else -> -1
                    }
                }
            } else mutateChance += mutationFactor
        }
    }

    private val creationQueue = LinkedList<Int>()
    private val distanceMap = HashMap<Int, Int>()
    private fun createArea(
        terrainType: TerrainType,
        minRadius: Int = 1,
        maxRadius: Int = min(columns, rows) / 2
    ) {
        val start = map[random.nextInt(columns)][random.nextInt(rows)]
        creationQueue.addLast(start)
        distanceMap[start] = 1
        while (creationQueue.isNotEmpty()) {
            val tile = creationQueue.removeFirst()
            val distance = distanceMap[tile]!!
            replaceTile(tile, terrainType)
            if (distance >= maxRadius) continue
            val coord = mTile[tile].coord
            val chance = when {
                distance < minRadius -> 1f
                minRadius == maxRadius -> 0f
                else -> 1f - (distance + 1 - minRadius) / (maxRadius - minRadius).toFloat()
            }
            if (chance <= 0f) continue
            for (diff in -1..1 step 2) {
                for (i in 0..1) {
                    var x = coord.x
                    var y = coord.y
                    if (i == 0) x += diff else y += diff
                    if (x < 0 || y < 0 || x >= columns || y >= rows) continue
                    val next = map[x][y]
                    if (distanceMap.containsKey(next)) continue
                    if (chance >= 1f || random.nextFloat() < chance) {
                        creationQueue.add(next)
                        distanceMap[next] = distance + 1
                    }
                }
            }
        }
        distanceMap.clear()
    }

    private inline fun forNeighbor(x: Int, y: Int, function: (tile: Int) -> Boolean) {
        for (diff in -1..1 step 2) {
            for (i in 0..1) {
                var x1 = x
                var y1 = y
                if (i == 0) x1 += diff else y1 += diff
                if (x1 < 0 || y1 < 0 || x1 >= columns || y1 >= rows) continue
                val tile = map[x1][y1]
                if (function(tile)) break
            }
        }
    }

    private fun createPath(vertical: Boolean = false) {
        val ref = if (vertical) columns else rows
        var centerFactor = 2f
        var start = -1
        var reversed = random.nextBoolean()
        for (i in 1..20) {
            val startRange = (ref / centerFactor).toInt()
            val x = if (reversed) ref - 1 else 0
            val y = random.nextInt(startRange) + (ref - startRange) / 2
            start = getTile(x, y, vertical)
            if (start < 0) continue
            var ideal = true
            forNeighbor(if (vertical) y else x, if (vertical) x else y) {
                val cTile = mTile[it]
                if (cTile.terrainType == TerrainType.WATER || cTile.obstacle >= 0) {
                    ideal = false
                    true
                } else false
            }
            if (!ideal) continue
            val cTile = mTile[start]
            if (cTile.terrainType != TerrainType.WATER && cTile.obstacle < 0) break
            reversed = !reversed
            centerFactor -= 0.2f
            centerFactor = centerFactor.coerceAtLeast(1f)
        }

        var minCost = Float.MAX_VALUE
        var end: PathNode? = null
        val area = pathfinderSystem.area(start, null, diagonal = false, buildingRoad = true)
        for (node in area) {
            val coord = mTile[node.tile].coord
            val endX = !vertical && coord.x == (if (reversed) 0 else columns - 1)
            val endY = vertical && coord.y == (if (reversed) 0 else rows - 1)
            if (node.cost < minCost && (endX || endY)) {
                minCost = node.cost
                end = node
            }
        }
        buildRoad(end?.tile ?: return)
        while (end?.prev != null) {
            val coord = mTile[end.tile].coord
            end = end.prev!!
            val cTile = mTile[end.tile]
            val endX = reversed && coord.x == columns - 1 || !reversed && coord.x == 0
            val endY = reversed && coord.y == rows - 1 || !reversed && coord.y == 0
            if (!vertical && endX && cTile.coord.x == coord.x) break
            if (vertical && endY && cTile.coord.y == coord.y) break
            buildRoad(end.tile)
        }
    }

    private fun replaceTile(entityId: Int, terrainType: TerrainType) {
        val tileComponent = mTile[entityId]
        tileComponent.terrainType = terrainType
    }

    private fun buildRoad(entityId: Int) {
        mTile[entityId].hasRoad = true
    }

    private fun getEmptyTile(): Int {
        while (emptyTiles.size() > 0) {
            val tile = emptyTiles.remove(random.nextInt(emptyTiles.size()))
            val cTile = mTile[tile]
            if (cTile.obstacle >= 0) continue
            return tile
        }
        return -1
    }

    private fun createObstacles(amount: Int) {
        var count = 0
        while (count < amount) {
            val tile = getEmptyTile()
            if (tile < 0) break
            val cTile = mTile[tile]
            var obstacleType = ObstacleType.ROCK
            var destroyable = false
            when (cTile.terrainType) {
                TerrainType.DIRT, TerrainType.NONE, TerrainType.WATER -> {
                    obstacleType = ObstacleType.ROCK
                }
                TerrainType.GRASS -> {
                    obstacleType = ObstacleType.TREE
                    destroyable = true
                }
            }
            val obstacle = world.create()
            mObstacle.create(obstacle).apply {
                type = obstacleType
                this.destroyable = destroyable
                maxHp = 100f
                hp = maxHp
            }
            count += 1
            cTile.obstacle = obstacle
        }
    }

    private fun clean() {
        emptyTiles.clear()
    }

    fun generate(): Array<IntArray> {
        Logger.startWatch("Map Generation")
        if (!initialized) init()
        repeat(random.nextInt(5) + 5) {
            createArea(TerrainType.fromByte((random.nextLong(2) + 1).toByte())!!, min(columns, rows) / 3)
        }
        repeat(random.nextInt(3)) {
            createArea(TerrainType.WATER, 2, 5)
        }
        var vertical = random.nextBoolean()
        var riverCount = 0
        repeat(random.nextInt(4)) {
            createLine(TerrainType.WATER, vertical = vertical, fork = true)
            vertical = !vertical
            riverCount++
        }
        repeat(random.nextInt(3)) {
            createArea(TerrainType.WATER, 2, 5)
        }
        createObstacles(random.nextInt(columns * rows / 20) + columns * rows / 40)
        repeat((random.nextInt(2) + riverCount).coerceIn(1, 2)) {
            createPath(vertical)
            vertical = !vertical
        }
        clean()
        Logger.stopWatch("Map Generation")
        return map
    }
}
