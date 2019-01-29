package net.natruid.jungle.utils

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.Queue
import ktx.ashley.add
import ktx.ashley.entity
import ktx.collections.GdxArray
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.systems.TileSystem.Companion.tileSize
import java.text.DecimalFormat

class AreaIndicator : Pool.Poolable {
    companion object {
        private val formatter = DecimalFormat("#.#")

        fun obtain(engine: Engine, pathFinderResult: GdxArray<PathNode>): AreaIndicator {
            val ret = Pools.obtain(AreaIndicator::class.java)
            ret.engine = engine
            ret.areaResult = pathFinderResult
            return ret
        }
    }

    private var engine: Engine? = null
    private var areaResult: GdxArray<PathNode>? = null
    private val entities = GdxArray<Entity>()
    private val pathEntities = GdxArray<Entity>()
    private var shown: Boolean = false

    fun setup(engine: Engine, pathResult: GdxArray<PathNode>) {
        reset()
        this.engine = engine
        this.areaResult = pathResult
    }

    fun show() {
        if (shown) return
        val engine = engine ?: return
        val result = areaResult ?: return
        val tiles = engine.getSystem(TileSystem::class.java) ?: return

        if (entities.size > 0) {
            entities.forEach {
                it.getComponent(TransformComponent::class.java)?.visible = true
            }
        } else {
            for (p in result) {
                p.tile?.let { tile ->
                    engine.add {
                        val e = entity {
                            with<TransformComponent> {
                                position.set(tiles.getPosition(tile))
                            }
                            with<RectComponent> {
                                width = TileSystem.tileSize.toFloat()
                                height = tileSize.toFloat()
                                type = ShapeRenderer.ShapeType.Filled
                                color.set(Color.CYAN).a = 0.3f
                            }
                            with<LabelComponent> {
                                text = formatter.format(p.length)
                                align = Align.center
                            }
                        }
                        this@AreaIndicator.entities.add(e)
                    }
                }
            }
        }

        shown = true
    }

    fun hide() {
        if (!shown) return
        entities.forEach {
            it.getComponent(TransformComponent::class.java)?.visible = false
        }
        shown = false
    }

    private val pathQueue = Queue<TileComponent>()

    private fun getPathQueue(x: Int, y: Int): Queue<TileComponent>? {
        val result = areaResult ?: return null
        for (node in result) {
            if (x == node.tile!!.x && y == node.tile!!.y) {
                pathQueue.clear()
                var prevNode = node
                while (prevNode != null) {
                    pathQueue.addFirst(prevNode.tile)
                    prevNode = prevNode.prev
                }
                return pathQueue
            }
        }

        return null
    }

    fun getPathTo(x: Int, y: Int): Array<TileComponent>? {
        val queue = getPathQueue(x, y) ?: return null

        return Array(queue.size) {
            queue.removeFirst()
        }
    }

    fun showPathTo(x: Int, y: Int): Boolean {
        val tiles = engine!!.getSystem(TileSystem::class.java)!!
        val queue = getPathQueue(x, y) ?: return false
        while (!queue.isEmpty) {
            engine!!.add {
                val tile = queue.removeFirst()
                val e = entity {
                    with<TransformComponent> {
                        position.set(tiles.getPosition(tile))
                    }
                    with<RectComponent> {
                        width = tileSize.toFloat()
                        height = tileSize.toFloat()
                        type = ShapeRenderer.ShapeType.Filled
                        color.set(Color.CYAN).a = 0.3f
                    }
                }
                pathEntities.add(e)
            }
        }
        return true
    }

    fun clearPath() {
        for (entity in pathEntities) {
            engine!!.removeEntity(entity)
        }
        pathEntities.clear()
    }

    override fun reset() {
        if (engine != null && entities.size > 0) {
            entities.forEach { entity ->
                engine!!.removeEntity(entity)
            }
            entities.clear()
        }
        clearPath()
        Pathfinder.freeResult(this.areaResult)
        engine = null
        areaResult = null
        shown = false
    }

    fun free() {
        Pools.free(this)
    }
}