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
import ktx.collections.GdxArray
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class TileSystem : EntitySystem(), InputProcessor {
    companion object {
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

    var columns = 0
        private set
    val rows: Int
        get() {
            return tiles.size / columns
        }

    private val tileSize = 64
    private val halfTileSize = tileSize / 2

    private val family = allOf(TileComponent::class, TransformComponent::class).get()
    private var tiles: GdxArray<TileComponent> = GdxArray(1600)
    private val mouseCoord: Point = Point()
    private var mouseOnTile: Entity? = null
    private var mouseOnTileTransform: TransformComponent? = null
    private var gridRenderer: Entity? = null
    private val renderer = Jungle.instance.renderer
    private val shapeRenderer = renderer.shapeRenderer
    private val camera = Jungle.instance.camera
    private val gridColor = Color(0.5f, 0.7f, 0.3f, 0.8f)
    private val gridRenderCallback: ((TransformComponent) -> Unit) = { renderGrid() }

    operator fun get(x: Int, y: Int): TileComponent? {
        if (x < 0 || y < 0 || x >= columns || y >= columns) return null
        val index = y * columns + x
        if (index < 0 || index >= tiles.size) return null
        return tiles[index]
    }

    operator fun get(coord: Point?): TileComponent? {
        if (coord == null) return null
        return get(coord.x, coord.y)
    }

    fun getEntity(x: Int, y: Int): Entity? {
        if (columns == 0) return null
        val e = engine?.getEntitiesFor(family) ?: return null
        val index = y * columns + x
        if (index < 0 || index >= e.size()) return null
        return e[index]
    }

    fun getEntity(tile: TileComponent): Entity? {
        return getEntity(tile.x, tile.y)
    }

    fun getPosition(tile: TileComponent): Vector3? {
        return getEntity(tile)?.getComponent(TransformComponent::class.java)?.position
    }

    private val gdxArrayForNeighbors = GdxArray<TileComponent>(8)
    fun neighbors(x: Int, y: Int, diagonal: Boolean = false): Array<TileComponent> {
        gdxArrayForNeighbors.clear()
        val p = Point.obtain()
        for (point in if (!diagonal) adjacent else diagonals) {
            p.set(x, y)
            p += point
            val tile = get(p) ?: continue
            gdxArrayForNeighbors.add(tile)
        }
        p.free()
        return gdxArrayForNeighbors.toArray(TileComponent::class.java)
    }

    fun create(columns: Int, rows: Int) {
        val e = engine ?: return
        clean()
        this.columns = columns
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val isBlock = random() > 0.8 && x > 0 && y > 0
                e.add {
                    entity {
                        val tile = with<TileComponent> {
                            this.x = x
                            this.y = y
                            walkable = !isBlock
                        }
                        tiles.add(tile)
                        with<TransformComponent> {
                            position.x = x * tileSize.toFloat()
                            position.y = y * tileSize.toFloat()
                        }
                        with<RectComponent> {
                            width = tileSize.toFloat()
                            height = tileSize.toFloat()
                            if (!isBlock) {
                                color.set(random() * 0.1f + 0.2f, random() * 0.1f + 0.4f, 0f, 1f)
                            } else {
                                color.set(random() * 0.1f, 0f, random() * 0.7f + 0.1f, 1f)
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
                    color.set(Color.YELLOW)
                }
            }
        }
    }

    private fun renderGrid() {
        if (columns <= 0) return

        renderer.begin(camera, RendererHelper.Type.ShapeRenderer, ShapeRenderer.ShapeType.Line)
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

    private val vecForProjection = Vector3()
    private fun screenToCoord(screenCoord: Point): Boolean {
        if (columns == 0) return false

        val camera = Jungle.instance.camera
        vecForProjection.set(screenCoord.x.toFloat(), screenCoord.y.toFloat(), 0f)
        camera.unproject(vecForProjection)
        val x = vecForProjection.x.roundToInt() + halfTileSize
        val y = vecForProjection.y.roundToInt() + halfTileSize
        if (x < 0 || x > columns * tileSize || y < 0 || y > rows * tileSize) return false

        screenCoord.set(x / tileSize, y / tileSize)
        return true
    }

    private val pathEntities = GdxArray<Entity>()
    private var pathResult: GdxArray<PathNode>? = null
    private val formatter = DecimalFormat("#.#")
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val tile = get(mouseCoord.x, mouseCoord.y)
        if (tile != null) {
            cancelPath()
            pathResult = Pathfinder.area(this, tile, 5f)
            pathResult?.let { result ->
                for (p in result) {
                    p.tile?.let { tile ->
                        engine.add {
                            val e = entity {
                                with<TransformComponent> {
                                    position.set(getPosition(tile))
                                }
                                with<RectComponent> {
                                    color.set(Color.GREEN)
                                    width = tileSize.toFloat()
                                    height = tileSize.toFloat()
                                }
                                with<LabelComponent> {
                                    text = formatter.format(p.length)
                                }
                            }
                            pathEntities.add(e)
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    private fun cancelPath() {
        for (pathEntity in pathEntities) {
            engine.removeEntity(pathEntity)
        }
        pathEntities.clear()
        pathResult?.let { Pathfinder.freeResult(it) }
        pathResult = null
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    private val pointForMouseMoved = Point()
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!this.checkProcessing()) return false

        pointForMouseMoved.set(screenX, screenY)
        if (!screenToCoord(pointForMouseMoved)) pointForMouseMoved.setToNone()
        if (pointForMouseMoved == mouseCoord) return false
        val mott = mouseOnTileTransform ?: return false

        mouseCoord.set(pointForMouseMoved)
        if (!mouseCoord.hasValue) {
            mott.visible = false
            return false
        }
        mott.visible = true
        mott.position.set((mouseCoord.x * tileSize).toFloat(), (mouseCoord.y * tileSize).toFloat(), 10f)
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
        pathEntities.clear()
        pathResult = null
    }
}