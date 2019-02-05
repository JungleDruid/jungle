package net.natruid.jungle.utils

import com.badlogic.gdx.math.RandomXS128
import net.natruid.jungle.components.TileComponent

class MapGenerator(private val columns: Int, private val rows: Int) {
    private val map = Array(columns) { x -> Array(rows) { y -> TileComponent(Point(x, y)) } }
    val random = RandomXS128()

    private fun createLine(
        terrainType: TileComponent.TerrainType = TileComponent.TerrainType.ROAD,
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
            if (fork) {
                if (vertical && map[wMid][l].terrainType == terrainType
                    || !vertical && map[l][wMid].terrainType == terrainType)
                    break
            }
            for (w in 0 until width) {
                if (vertical)
                    map[wMid + w - width / 2][l].terrainType = terrainType
                else
                    map[l][wMid + w - width / 2].terrainType = terrainType
            }
            if (random.nextLong(100) >= 100L - mutateChance) {
                mutateChance = 0
                if (random.nextBoolean()) {
                    if (random.nextBoolean()) {
                        if (wMid < ref - 1) {
                            wMid += 1
                            if (vertical)
                                map[wMid + width / 2 - 1 + width.rem(2)][l].terrainType = terrainType
                            else
                                map[l][wMid + width / 2 - 1 + width.rem(2)].terrainType = terrainType
                        }
                    } else {
                        if (wMid > 0) {
                            wMid -= 1
                            if (vertical)
                                map[wMid - width / 2][l].terrainType = terrainType
                            else
                                map[l][wMid - width / 2].terrainType = terrainType
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

    private fun createRect(terrainType: TileComponent.TerrainType, maxWidth: Int = columns, maxHeight: Int = rows) {
        val left = random.nextInt(columns)
        val right = random.nextInt(columns - left).coerceAtMost(maxWidth - 1) + left
        val bottom = random.nextInt(rows)
        val top = random.nextInt(rows - bottom).coerceAtMost(maxHeight - 1) + bottom
        for (x in left..right) {
            for (y in bottom..top) {
                map[x][y].terrainType = terrainType
            }
        }
    }

    fun get(): Array<Array<TileComponent>> {
        repeat(random.nextInt(200) + 100) {
            createRect(TileComponent.TerrainType.fromByte((random.nextLong(2) + 1).toByte())!!)
        }
        repeat(random.nextInt(5)) {
            createRect(TileComponent.TerrainType.WATER, 5, 5)
        }
        val vertical = random.nextBoolean()
        createLine(TileComponent.TerrainType.WATER, vertical = vertical)
        createLine(vertical = !vertical)
        return map
    }
}
