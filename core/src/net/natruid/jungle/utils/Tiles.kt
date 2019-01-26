package net.natruid.jungle.utils

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent
import java.lang.Math.random

class Tiles : Pool.Poolable {
    companion object {
        fun obtain(engine: PooledEngine?): Tiles {
            val tiles = Pools.obtain(Tiles::class.java)
            tiles.engine = engine
            return tiles
        }
    }

    private val list = ArrayList<Entity>()
    var engine: PooledEngine? = null
        private set
    var columns = 0
        private set
    val rows get() = list.size / columns

    operator fun get(x: Int, y: Int): Entity? {
        val index = y * columns + x
        if (index < 0 || index >= list.size) return null
        return list[index]
    }

    fun create(columns: Int, rows: Int) {
        list.clear()
        this.columns = columns
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val isBlock = random() > 0.8 && x > 0 && y > 0
                val entity = if (engine != null) {
                    engine!!.createEntity()
                } else {
                    Entity()
                }

                entity.add(createComponent<TileComponent> {
                    walkable = !isBlock
                })

                entity.add(createComponent<TransformComponent> {
                    position.x = x * 64f
                    position.y = y * 64f
                })

                entity.add(createComponent<RectComponent> {
                    width = 64f
                    height = 64f
                    color = if (!isBlock) {
                        Pools.obtain(Color::class.java).set(
                                random().toFloat() * 0.1f + 0.2f,
                                random().toFloat() * 0.1f + 0.4f,
                                0f,
                                1f
                        )
                    } else {
                        Pools.obtain(Color::class.java).set(
                                random().toFloat() * 0.1f,
                                0f,
                                random().toFloat() * 0.7f + 0.1f,
                                1f
                        )
                    }
                    type = ShapeRenderer.ShapeType.Filled
                })

                list.add(entity)
                engine?.addEntity(entity)
            }
        }
    }

    override fun reset() {
        list.clear()
        engine = null
        columns = 0
    }

    fun free() {
        Pools.free(this)
    }

    private inline fun <reified T : Component> createComponent(configure: T.() -> Unit = {}): T {
        val component = if (engine != null) {
            engine!!.createComponent(T::class.java)
        } else {
            T::class.java.getDeclaredConstructor().newInstance()
        }
        component.configure()
        return component
    }
}
