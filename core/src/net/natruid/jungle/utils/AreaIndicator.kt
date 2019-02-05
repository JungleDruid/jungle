package net.natruid.jungle.utils

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import ktx.ashley.add
import ktx.ashley.entity
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.systems.TileSystem.Companion.tileSize
import java.text.DecimalFormat

class AreaIndicator(private var engine: Engine, private var pathFinderResult: Collection<PathNode>) {
    companion object {
        private val formatter = DecimalFormat("#.#")
    }

    private val areaEntities = ArrayList<Entity>()
    private val pathEntities = ArrayList<Entity>()
    private var shown: Boolean = false
    private val moveAreaColor = Color(0f, 1f, 1f, .4f)

    fun show() {
        if (shown) return
        val tiles = engine.getSystem(TileSystem::class.java) ?: return

        if (areaEntities.size > 0) {
            areaEntities.forEach {
                it.getComponent(TransformComponent::class.java)?.visible = true
            }
        } else {
            for (p in pathFinderResult) {
                p.tile.let { tile ->
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
                                text = formatter.format(p.cost)
                                align = Align.center
                                fontName = "big"
                            }
                        }
                        areaEntities.add(e)
                    }
                }
            }
        }

        shown = true
    }

    fun hide() {
        if (!shown) return
        areaEntities.forEach {
            it.getComponent(TransformComponent::class.java)?.visible = false
        }
        shown = false
    }

    fun getPathTo(coord: Point): Array<TileComponent>? {
        return Pathfinder.extractPath(pathFinderResult, coord)
    }

    fun showPathTo(coord: Point): Boolean {
        val tiles = engine.getSystem(TileSystem::class.java)!!
        val path = getPathTo(coord) ?: return false
        for (tile in path) {
            engine.add {
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
        if (areaEntities.size > 0) {
            areaEntities.forEach { entity ->
                engine.removeEntity(entity)
            }
            areaEntities.clear()
        }
        clearPath()
        shown = false
    }
}