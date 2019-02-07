package net.natruid.jungle.utils

import com.artemis.ArchetypeBuilder
import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.math.RandomXS128
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TileComponent.TerrainType
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.PathfinderSystem
import kotlin.math.min

class MapGenerator(private val columns: Int, private val rows: Int, private val world: World) {
    private val tileArchetype = ArchetypeBuilder().add(
        TileComponent::class.java,
        TransformComponent::class.java,
        TextureComponent::class.java
    ).build(world)
    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var sPathfinder: PathfinderSystem
    private lateinit var map: Array<IntArray>
    val random = RandomXS128()

    fun init(): Array<IntArray> {
        map = Array(columns) { x ->
            IntArray(rows) { y ->
                val entityId = world.create(tileArchetype)
                mTile[entityId].coord.set(x, y)
                entityId
            }
        }
        return map
    }

    private fun getTile(x: Int, y: Int, reversed: Boolean = false): Int {
        return if (!reversed) {
            map[x][y]
        } else {
            map[y][x]
        }
    }

    private fun createLine(
        terrainType: TileComponent.TerrainType = TerrainType.ROAD,
        minWidth: Int = 1,
        maxWidth: Int = 3,
        vertical: Boolean = random.nextBoolean(),
        fork: Boolean = false
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
                val cTile: TileComponent = mTile[getTile(l, wMid + w - width / 2, vertical)]
                if (cTile.terrainType == TerrainType.WATER && terrainType == TerrainType.ROAD) {
                    noMutation = true
                }
                cTile.terrainType = terrainType
            }
            if (noMutation) continue
            if (random.nextLong(100) >= 100L - mutateChance) {
                mutateChance = 0
                if (random.nextBoolean()) {
                    if (random.nextBoolean()) {
                        if (wMid < ref - 1) {
                            wMid += 1
                            mTile[getTile(l, wMid + width / 2 - 1 + width.rem(2), vertical)].terrainType = terrainType
                        }
                    } else {
                        if (wMid > 0) {
                            wMid -= 1
                            mTile[getTile(l, wMid - width / 2, vertical)].terrainType = terrainType
                        }
                    }
                } else {
                    width += when {
                        width == maxWidth -> -1
                        width == minWidth || random.nextBoolean() -> 1
                        else -> -1
                    }
                }
            } else mutateChance += 2L
        }
    }

    private fun createRect(
        terrainType: TileComponent.TerrainType,
        minWidth: Int = 1,
        maxWidth: Int = min(columns, rows)
    ) {
        val left = random.nextInt(columns - minWidth)
        val right = random.nextInt(columns - left - minWidth + 1).coerceAtMost(maxWidth - minWidth) + left + minWidth - 1
        val bottom = random.nextInt(rows - minWidth)
        val top = random.nextInt(rows - bottom - minWidth + 1).coerceAtMost(maxWidth - minWidth) + bottom + minWidth - 1
        for (x in left..right) {
            for (y in bottom..top) {
                mTile[map[x][y]].terrainType = terrainType
            }
        }
    }

    private fun createPath(vertical: Boolean = false) {
        val ref = if (vertical) columns else rows
        val startRange = ref / 2
        var start = -1
        for (i in 1..10) {
            start = getTile(0, random.nextInt(startRange) + (ref - startRange) / 2, vertical)
            val cTile = mTile[start]
            if (cTile.terrainType != TerrainType.WATER && cTile.walkable) break
        }

        var minCost = (columns * rows).toFloat()
        var end: PathNode? = null
        val area = sPathfinder.area(start, minCost, false)
        for (node in area) {
            val coord = mTile[node.tile].coord
            if (node.cost < minCost && (vertical && coord.y == rows - 1 || !vertical && coord.x == columns - 1)) {
                minCost = node.cost
                end = node
            }
        }
        mTile[end?.tile ?: return].terrainType = TerrainType.ROAD
        while (end?.prev != null) {
            val coord = mTile[end.tile].coord
            end = end.prev!!
            val cTile = mTile[end.tile]
            if (vertical && coord.y == 0 && cTile.coord.y == coord.y) break
            if (!vertical && coord.x == 0 && cTile.coord.x == coord.x) break
            cTile.terrainType = TerrainType.ROAD
        }
    }

    fun generate(): Array<IntArray> {
        repeat(random.nextInt(200) + 100) {
            createRect(TileComponent.TerrainType.fromByte((random.nextLong(2) + 1).toByte())!!)
        }
        repeat(random.nextInt(3)) {
            createRect(TerrainType.WATER, 2, 5)
        }
        var vertical = random.nextBoolean()
        repeat(random.nextInt(4)) {
            createLine(TerrainType.WATER, vertical = vertical, fork = true)
            vertical = !vertical
        }
        repeat(random.nextInt(3)) {
            createRect(TerrainType.WATER, 2, 5)
        }
        createPath(vertical)
        repeat(random.nextInt(2)) {
            vertical = !vertical
            createLine(vertical = vertical, fork = random.nextBoolean())
        }
        return map
    }
}
