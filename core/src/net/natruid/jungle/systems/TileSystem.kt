package net.natruid.jungle.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.utils.Pools
import ktx.ashley.add
import ktx.ashley.allOf
import ktx.ashley.entity
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent

class TileSystem : EntitySystem() {
    var columns = 0
        private set

    private val family = allOf(TileComponent::class, TransformComponent::class).get()
    private var tiles: ImmutableArray<Entity>? = null

    operator fun get(x: Int, y: Int): Entity? {
        tiles ?: return null
        val index = y * columns + x
        if (index < 0 || index >= tiles!!.size()) return null
        return tiles!![index]
    }

    fun create(columns: Int, rows: Int) {
        if (engine == null) return
        this.columns = columns
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val isBlock = random() > 0.8 && x > 0 && y > 0
                engine.add {
                    entity {
                        with<TileComponent> {
                            walkable = !isBlock
                        }
                        with<TransformComponent> {
                            position.x = x * 64f
                            position.y = y * 64f
                        }
                        with<RectComponent> {
                            width = 64f
                            height = 64f
                            color = if (!isBlock) {
                                Pools.obtain(Color::class.java).set(
                                        random() * 0.1f + 0.2f,
                                        random() * 0.1f + 0.4f,
                                        0f,
                                        1f
                                )
                            } else {
                                Pools.obtain(Color::class.java).set(
                                        random() * 0.1f,
                                        0f,
                                        random() * 0.7f + 0.1f,
                                        1f
                                )
                            }
                            type = ShapeRenderer.ShapeType.Filled
                        }
                    }
                }
            }
        }
        tiles = engine.getEntitiesFor(family)
    }
}