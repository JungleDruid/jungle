package net.natruid.jungle.utils

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Queue
import ktx.ashley.add
import ktx.ashley.entity
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.systems.TileSystem.Companion.tileSize
import java.text.DecimalFormat

class AreaIndicator(private var engine: Engine, private var pathFinderResult: ArrayList<PathNode>) {
    companion object {
        private val formatter = DecimalFormat("#.#")
    }

    private val entities = ArrayList<Entity>()
    private val pathEntities = ArrayList<Entity>()
    private var shown: Boolean = false
    private val moveAreaColor = Color(0f, 1f, 1f, .3f)

    fun show() {
        if (shown) return
        val tiles = engine.getSystem(TileSystem::class.java) ?: return

        if (entities.size > 0) {
            entities.forEach {
                it.getComponent(TransformComponent::class.java)?.visible = true
            }
        } else {
            for (p in pathFinderResult) {
                p.tile?.let { tile ->
                    engine.add {
                        val e = entity {
                            with<TransformComponent> {
                                position = tiles.getPosition(tile)!!
                            }
                            with<RectComponent> {
                                width = TileSystem.tileSize.toFloat()
                                height = tileSize.toFloat()
                                type = ShapeRenderer.ShapeType.Filled
                                color = moveAreaColor
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

    private fun getPathQueue(coord: Point): Queue<TileComponent>? {
        for (node in pathFinderResult) {
            if (coord == node.tile!!.coord) {
                pathQueue.clear()
                var prevNode: PathNode? = node
                while (prevNode != null) {
                    pathQueue.addFirst(prevNode.tile)
                    prevNode = prevNode.prev
                }
                return pathQueue
            }
        }

        return null
    }

    fun getPathTo(coord: Point): Array<TileComponent>? {
        val queue = getPathQueue(coord) ?: return null

        return Array(queue.size) {
            queue.removeFirst()
        }
    }

    fun showPathTo(coord: Point): Boolean {
        val tiles = engine.getSystem(TileSystem::class.java)!!
        val queue = getPathQueue(coord) ?: return false
        while (!queue.isEmpty) {
            engine.add {
                val tile = queue.removeFirst()
                val e = entity {
                    with<TransformComponent> {
                        position = tiles.getPosition(tile)!!
                    }
                    with<RectComponent> {
                        width = tileSize.toFloat()
                        height = tileSize.toFloat()
                        type = ShapeRenderer.ShapeType.Filled
                        color = moveAreaColor
                    }
                }
                pathEntities.add(e)
            }
        }
        return true
    }

    fun clearPath() {
        for (entity in pathEntities) {
            engine.removeEntity(entity)
        }
        pathEntities.clear()
    }

    fun clear() {
        if (entities.size > 0) {
            entities.forEach { entity ->
                engine.removeEntity(entity)
            }
            entities.clear()
        }
        clearPath()
        shown = false
    }
}