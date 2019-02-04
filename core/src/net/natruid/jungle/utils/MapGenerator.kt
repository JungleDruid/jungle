package net.natruid.jungle.utils

import com.badlogic.gdx.math.RandomXS128
import net.natruid.jungle.components.TileComponent

class MapGenerator(private val columns: Int, private val rows: Int) {
    private val map = Array(columns) { x -> Array(rows) { y -> TileComponent(Point(x, y)) } }
    private val random = RandomXS128()

    private fun createRoad() {
        val range = rows / 2
        var y = random.nextInt(range) + (rows - range) / 2
        var width = random.nextInt(3) + 1
        for (x in 0 until columns) {
            for (i in 0 until width) {
                map[x][y + i - width / 2].terrainType = TileComponent.TerrainType.ROAD
            }
            if (random.nextLong(100) >= 90L) {
                if (random.nextBoolean()) {
                    if (y < rows - 1) {
                        y += 1
                        map[x][y + width / 2 - 1 + width.rem(2)].terrainType = TileComponent.TerrainType.ROAD
                    }
                } else {
                    if (y > 0) {
                        y -= 1
                        map[x][y - width / 2].terrainType = TileComponent.TerrainType.ROAD
                    }
                }
                y = y.coerceIn(0, rows - 1)
            }
            if (random.nextLong(100) >= 95L) {
                width += if (random.nextBoolean()) 1 else -1
                width = width.coerceIn(1, 3)
            }
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
        createRoad()
        return map
    }
}