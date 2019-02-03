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

    fun get(): Array<Array<TileComponent>> {
        createRoad()
        return map
    }
}