package net.natruid.jungle.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Vector3
import ktx.ashley.add
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.math.vec3
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.RenderableComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.ImmutablePoint
import net.natruid.jungle.utils.Point
import net.natruid.jungle.utils.RendererHelper
import kotlin.math.roundToInt

class TileSystem : EntitySystem(), InputProcessor {
    companion object {
        const val tileSize = 64

        private val adjacent = arrayOf(
                ImmutablePoint(Point(1, 0)),
                ImmutablePoint(Point(0, 1)),
                ImmutablePoint(Point(-1, 0)),
                ImmutablePoint(Point(0, -1))
        )
        private val diagonals = arrayOf(
                ImmutablePoint(Point(1, 1)),
                ImmutablePoint(Point(-1, 1)),
                ImmutablePoint(Point(-1, -1)),
                ImmutablePoint(Point(1, -1))
        )
    }

    private var columns = 0
    private var rows = 0
    var mouseCoord: Point? = null

    private val halfTileSize = tileSize / 2

    private val family = allOf(TileComponent::class, TransformComponent::class).get()
    private val tiles by lazy { ArrayList<TileComponent>(columns * rows) }
    private var mouseOnTile: Entity? = null
    private var mouseOnTileTransform: TransformComponent? = null
    private var gridRenderer: Entity? = null
    private val renderer = Jungle.instance.renderer
    private val shapeRenderer = renderer.shapeRenderer
    private val camera = Jungle.instance.camera
    private val gridColor = Color(0.5f, 0.7f, 0.3f, 0.8f)
    private val gridRenderCallback: ((TransformComponent) -> Unit) = { renderGrid() }
    private val mouseOnTileColor = Color(1f, 1f, 0f, 0.4f)

    operator fun get(x: Int, y: Int): TileComponent? {
        if (!isCoordValid(x, y)) return null
        val index = y * columns + x
        if (index < 0 || index >= tiles.size) return null
        return tiles[index]
    }

    operator fun get(coord: Point?): TileComponent? {
        if (coord == null) return null
        return get(coord.x, coord.y)
    }

    fun isCoordValid(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < columns && y < rows
    }

    fun isCoordValid(coord: Point?): Boolean {
        if (coord == null) return false
        return coord.x >= 0 && coord.y >= 0 && coord.x < columns && coord.y < rows
    }

    fun getEntity(tile: TileComponent): Entity? {
        if (columns == 0) return null
        val e = engine?.getEntitiesFor(family) ?: return null
        val index = tile.coord.y * columns + tile.coord.x
        if (index < 0 || index >= e.size()) return null
        return e[index]
    }

    fun getPosition(tile: TileComponent): Vector3? {
        return getEntity(tile)?.getComponent(TransformComponent::class.java)?.position
    }

    fun neighbors(coord: Point, diagonal: Boolean = false): Array<TileComponent?> {
        val array = Array<TileComponent?>(4) { null }
        for ((count, point) in (if (!diagonal) adjacent else diagonals).withIndex()) {
            val tile = get(coord + point)
            array[count] = tile
        }
        return array
    }

    fun create(columns: Int, rows: Int) {
        val e = engine ?: return
        clean()
        this.columns = columns
        this.rows = rows
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val isBlock = random() > 0.8 && x > 0 && y > 0
                e.add {
                    entity {
                        val tile = with<TileComponent> {
                            coord = Point(x, y)
                            walkable = !isBlock
                        }
                        tiles.add(tile)
                        with<TransformComponent> {
                            position = vec3(x * tileSize.toFloat(), y * tileSize.toFloat())
                        }
                        with<RectComponent> {
                            width = tileSize.toFloat()
                            height = tileSize.toFloat()
                            color = if (!isBlock) {
                                Color(random() * 0.1f + 0.2f, random() * 0.1f + 0.4f, 0f, 1f)
                            } else {
                                Color(random() * 0.1f, 0f, random() * 0.7f + 0.1f, 1f)
                            }
                            type = ShapeRenderer.ShapeType.Filled
                        }
                    }
                }
            }
        }
        e.add {
            gridRenderer = entity {
                with<TransformComponent> {
                    visible = false
                }
                with<RenderableComponent> {
                    renderCallback = this@TileSystem.gridRenderCallback
                }
            }
            mouseOnTile = entity {
                mouseOnTileTransform = with {
                    visible = false
                    position.z = 10f
                }
                with<RectComponent> {
                    width = tileSize.toFloat()
                    height = tileSize.toFloat()
                    type = ShapeRenderer.ShapeType.Filled
                    color = mouseOnTileColor
                }
            }
        }
    }

    private fun renderGrid() {
        if (columns <= 0) return

        renderer.begin(camera, RendererHelper.Type.SHAPE_RENDERER, ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = gridColor
        val origin = (-halfTileSize).toFloat()
        val right = (-halfTileSize + columns * tileSize).toFloat()
        val top = (-halfTileSize + rows * tileSize).toFloat()

        for (i in 0..columns) {
            shapeRenderer.line(
                    origin + i * tileSize,
                    origin,
                    origin + i * tileSize,
                    top
            )
        }
        for (i in 0..rows) {
            shapeRenderer.line(
                    origin,
                    origin + i * tileSize,
                    right,
                    origin + i * tileSize
            )
        }
    }

    private fun screenToCoord(screenX: Int, screenY: Int): Point? {
        if (columns == 0) return null

        val camera = Jungle.instance.camera
        val projection = vec3(screenX.toFloat(), screenY.toFloat(), 0f)
        camera.unproject(projection)
        val x = projection.x.roundToInt() + halfTileSize
        val y = projection.y.roundToInt() + halfTileSize
        if (x < 0 || x >= columns * tileSize || y < 0 || y >= rows * tileSize) return null

        return Point(x / tileSize, y / tileSize)
    }

    private fun screenToCoord(screenCoord: Point): Point? {
        return screenToCoord(screenCoord.x, screenCoord.y)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!this.checkProcessing()) return false

        val currentCoord = screenToCoord(Point(screenX, screenY))
        if (currentCoord == mouseCoord) return false
        val mott = mouseOnTileTransform ?: return false

        mouseCoord = currentCoord
        if (mouseCoord == null) {
            mott.visible = false
            return false
        }
        mott.visible = true
        mott.position = vec3((mouseCoord!!.x * tileSize).toFloat(), (mouseCoord!!.y * tileSize).toFloat(), 10f)
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.APOSTROPHE) {
            val gt = gridRenderer?.getComponent(TransformComponent::class.java)
            if (gt != null) {
                gt.visible = !gt.visible
            }
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun setProcessing(processing: Boolean) {
        super.setProcessing(processing)
        if (!processing) mouseOnTileTransform?.visible = false
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        clean()
    }

    private fun clean() {
        columns = 0
        tiles.clear()
    }
}