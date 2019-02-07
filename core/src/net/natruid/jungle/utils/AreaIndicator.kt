package net.natruid.jungle.utils

import com.artemis.World
import com.artemis.utils.IntBag
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.PathfinderSystem
import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.systems.TileSystem.Companion.tileSize
import net.natruid.jungle.utils.extensions.forEach
import java.text.DecimalFormat

class AreaIndicator(private var world: World, private var pathFinderResult: Array<PathNode>) {
    companion object {
        private val formatter = DecimalFormat("#.#")
    }

    private val sPathfinder = world.getSystem(PathfinderSystem::class.java)
    private val areaEntities = IntBag()
    private val pathEntities = IntBag()
    private val mTransform = world.getMapper(TransformComponent::class.java)
    private val mRect = world.getMapper(RectComponent::class.java)
    private val mLabel = world.getMapper(LabelComponent::class.java)
    private var shown: Boolean = false
    private val moveAreaColor = Color(0f, 1f, 1f, .4f)

    fun show() {
        if (shown) return

        if (areaEntities.size() > 0) {
            areaEntities.forEach {
                mTransform[it].visible = true
            }
        } else {
            for (p in pathFinderResult) {
                p.tile.let { tile ->
                    world.create().let { entityId ->
                        mTransform.create(entityId).position = mTransform[tile].position
                        mRect.create(entityId).apply {
                            width = TileSystem.tileSize.toFloat()
                            height = tileSize.toFloat()
                            type = ShapeRenderer.ShapeType.Filled
                            color = moveAreaColor
                        }
                        mLabel.create(entityId).apply {
                            text = formatter.format(p.cost)
                            align = Align.center
                            fontName = "big"
                        }
                        areaEntities.add(entityId)
                    }
                }
            }
        }

        shown = true
    }

    fun hide() {
        if (!shown) return
        areaEntities.forEach {
            mTransform[it].visible = false
        }
        shown = false
    }

    fun getPathTo(coord: Point): IntArray? {
        return sPathfinder.extractPath(pathFinderResult, coord)
    }

    fun showPathTo(coord: Point): Boolean {
        val path = getPathTo(coord) ?: return false
        for (tile in path) {
            world.create().let { entityId ->
                mTransform.create(entityId).position = mTransform[tile].position
                mRect.create(entityId).apply {
                    width = tileSize.toFloat()
                    height = tileSize.toFloat()
                    type = ShapeRenderer.ShapeType.Filled
                    color = moveAreaColor
                }
                pathEntities.add(entityId)
            }
        }
        return true
    }

    fun clearPath() {
        pathEntities.forEach {
            world.delete(it)
        }
        pathEntities.clear()
    }

    fun clear() {
        if (areaEntities.size() > 0) {
            areaEntities.forEach {
                world.delete(it)
            }
            areaEntities.clear()
        }
        clearPath()
        shown = false
    }
}
