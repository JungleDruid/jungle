package net.natruid.jungle.utils

import com.artemis.ArchetypeBuilder
import com.artemis.World
import com.badlogic.gdx.math.RandomXS128
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TileComponent.TerrainType
import net.natruid.jungle.components.TransformComponent

class MapGenerator(private val columns: Int, private val rows: Int, private val world: World) {
    private val tileArchetype = ArchetypeBuilder().add(
        TileComponent::class.java,
        TransformComponent::class.java,
        TextureComponent::class.java
    ).build(world)
    private val mTile = world.getMapper(TileComponent::class.java)
    private val map = Array(columns) { x ->
        IntArray(rows) { y ->
            val entityId = world.create(tileArchetype)
            mTile[entityId].coord.set(x, y)
            entityId
        }
    }
    val random = RandomXS128()

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
            if (fork) {
                if (vertical && mTile[map[wMid][l]].terrainType == terrainType
                    || !vertical && mTile[map[l][wMid]].terrainType == terrainType)
                    break
            }
            var noMutation = false
            for (w in 0 until width) {
                val cTile: TileComponent = if (vertical)
                    mTile[map[wMid + w - width / 2][l]]
                else
                    mTile[map[l][wMid + w - width / 2]]
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
                            if (vertical)
                                mTile[map[wMid + width / 2 - 1 + width.rem(2)][l]].terrainType = terrainType
                            else
                                mTile[map[l][wMid + width / 2 - 1 + width.rem(2)]].terrainType = terrainType
                        }
                    } else {
                        if (wMid > 0) {
                            wMid -= 1
                            if (vertical)
                                mTile[map[wMid - width / 2][l]].terrainType = terrainType
                            else
                                mTile[map[l][wMid - width / 2]].terrainType = terrainType
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
                mTile[map[x][y]].terrainType = terrainType
            }
        }
    }

    fun get(): Array<IntArray> {
        repeat(random.nextInt(200) + 100) {
            createRect(TileComponent.TerrainType.fromByte((random.nextLong(2) + 1).toByte())!!)
        }
        repeat(random.nextInt(5)) {
            createRect(TerrainType.WATER, 5, 5)
        }
        val vertical = random.nextBoolean()
        createLine(TerrainType.WATER, vertical = vertical)
        createLine(vertical = !vertical)
        return map
    }
}
