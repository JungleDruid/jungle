package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.*
import kotlin.math.roundToInt

class TileSystem : BaseSystem(), InputProcessor {
    companion object {
        const val tileSize = 64
        private const val TAG_GRID_RENDERER = "GridRenderer"
        private const val TAG_MOUSE_ON_TILE = "MouseOnTile"

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

    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mTexture: ComponentMapper<TextureComponent>
    private lateinit var mRect: ComponentMapper<RectComponent>
    private lateinit var mRenderable: ComponentMapper<RenderableComponent>
    private lateinit var mObstacle: ComponentMapper<ObstacleComponent>
    private lateinit var tileEntities: Array<IntArray>
    private lateinit var sTag: TagManager
    private var mouseOnTile
        get() = sTag.getEntityId(TAG_MOUSE_ON_TILE)
        set(value) {
            if (value >= 0) sTag.register(TAG_MOUSE_ON_TILE, value)
            else sTag.unregister(TAG_MOUSE_ON_TILE)
        }
    private val renderer = Jungle.instance.renderer
    private val shapeRenderer = renderer.shapeRenderer
    private val camera = Jungle.instance.camera
    private val gridColor = Color(0.5f, 0.7f, 0.3f, 0.8f)
    private val gridRenderCallback: ((TransformComponent) -> Unit) = { renderGrid() }
    private val mouseOnTileColor = Color(1f, 1f, 0f, 0.4f)
    private val tileShaderComponent by lazy {
        ShaderComponent(
            ShaderProgram(Scout["assets/shaders/vertex.glsl"], Scout["assets/shaders/fragment.glsl"]),
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE
        )
    }
    private val waterTileShaderComponent by lazy {
        ShaderComponent(
            ShaderProgram(Scout["assets/shaders/vertex.glsl"], Scout["assets/shaders/fragment.glsl"]),
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE
        )
    }

    operator fun get(coord: Point): Int {
        if (!isCoordValid(coord)) return -1
        return tileEntities[coord.x][coord.y]
    }

    fun isCoordValid(coord: Point?): Boolean {
        if (coord == null) return false
        return coord.x >= 0 && coord.y >= 0 && coord.x < columns && coord.y < rows
    }

    fun neighbors(coord: Point, diagonal: Boolean = false): IntArray {
        val array = IntArray(4) { -1 }
        for ((count, point) in (if (!diagonal) adjacent else diagonals).withIndex()) {
            val tile = get(coord + point)
            array[count] = tile
        }
        return array
    }

    private val dirtTexture = TextureRegion(Texture(Scout["assets/img/tiles/dirt.png"]))
    private val grassTexture = TextureRegion(Texture(Scout["assets/img/tiles/grass.png"]))
    private val roadTexture = TextureRegion(Texture(Scout["assets/img/tiles/road.png"]))
    private val waterTexture = TextureRegion(Texture(Scout["assets/img/tiles/water.png"]))
    private val bridgeTexture = TextureRegion(Texture(Scout["assets/img/tiles/bridge.png"]))
    private val treeTexture = TextureRegion(Texture(Scout["assets/img/tiles/tree.png"]))
    private val rockTexture = TextureRegion(Texture(Scout["assets/img/tiles/rock.png"]))

    fun create(columns: Int, rows: Int) {
        clean()
        this.columns = columns
        this.rows = rows
        val generator = MapGenerator(columns, rows, world)
        world.inject(generator)
        tileEntities = generator.init()
        generator.generate()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val tile = tileEntities[x][y]
                val cTile = mTile[tile]
                mTransform[tile].position = Vector2(x * tileSize.toFloat(), y * tileSize.toFloat())
                mTexture[tile].apply {
                    region = when (cTile.terrainType) {
                        TerrainType.NONE -> dirtTexture
                        TerrainType.DIRT -> dirtTexture
                        TerrainType.GRASS -> grassTexture
                        TerrainType.ROAD -> roadTexture
                        TerrainType.BRIDGE -> waterTexture
                        TerrainType.WATER -> waterTexture
                    }
                    color = when (cTile.terrainType) {
                        TerrainType.WATER ->
                            Color(1f, 1f, 1f, .7f)
                        TerrainType.ROAD ->
                            Color(1f, 1f, 1f, .93f + generator.random.nextFloat() * .07f)
                        TerrainType.BRIDGE ->
                            Color(1f, 1f, 1f, .7f)
                        else -> {
                            val gb = .8f + generator.random.nextFloat() * .2f
                            Color(
                                (gb + .2f).coerceAtMost(1f),
                                gb,
                                gb,
                                1f
                            )
                        }
                    }
                }
                if (cTile.terrainType == TerrainType.WATER)
                    world.edit(tile).add(waterTileShaderComponent)
                else
                    world.edit(tile).add(tileShaderComponent)
                if (cTile.terrainType == TerrainType.BRIDGE) {
                    world.create().let { bridge ->
                        mTransform.create(bridge).apply {
                            position = mTransform[tile].position
                            val compareTileX = if (x > 0) x - 1 else x + 1
                            if (mTile[tileEntities[compareTileX][y]].terrainType == TerrainType.WATER)
                                rotation = 90f
                        }
                        mTexture.create(bridge).region = bridgeTexture
                    }
                }

                if (cTile.obstacle >= 0) {
                    val obstacle = cTile.obstacle
                    val cObstacle = mObstacle[obstacle]
                    mTransform.create(obstacle).apply {
                        position = mTransform[tile].position
                        z = Constants.Z_OBSTACLE
                        scale.set(
                            generator.random.nextFloat() * 0.2f + 0.9f,
                            generator.random.nextFloat() * 0.2f + 0.9f
                        )
                    }
                    mTexture.create(obstacle).apply {
                        region = when (cObstacle.type) {
                            ObstacleType.TREE -> treeTexture
                            ObstacleType.ROCK -> rockTexture
                            else -> treeTexture
                        }
                    }
                }
            }
        }
        world.create().let { entityId ->
            sTag.register("GridRenderer", entityId)
            val visible = mTransform[entityId]?.visible ?: false
            mTransform.create(entityId).visible = visible
            mRenderable.create(entityId).renderCallback = gridRenderCallback
        }
        world.create().let { entityId ->
            mouseOnTile = entityId
            mTransform.create(entityId).apply {
                visible = false
                z = Constants.Z_MOUSE_ON_TILE
            }
            mRect.create(entityId).apply {
                width = tileSize.toFloat()
                height = tileSize.toFloat()
                type = ShapeRenderer.ShapeType.Filled
                color = mouseOnTileColor
            }
        }
    }

    private fun renderGrid() {
        if (columns <= 0) return

        renderer.begin(camera, RendererType.SHAPE_RENDERER, ShapeRenderer.ShapeType.Line)
        val origin = (-halfTileSize).toFloat()
        val right = (-halfTileSize + columns * tileSize).toFloat()
        val top = (-halfTileSize + rows * tileSize).toFloat()

        val shadowOffset = 1 * camera.zoom
        shapeRenderer.color = Color.DARK_GRAY
        for (i in 0..columns) {
            shapeRenderer.line(
                origin + i * tileSize + shadowOffset,
                origin - shadowOffset,
                origin + i * tileSize + shadowOffset,
                top - shadowOffset
            )
        }
        for (i in 0..rows) {
            shapeRenderer.line(
                origin + shadowOffset,
                origin + i * tileSize - shadowOffset,
                right + shadowOffset,
                origin + i * tileSize - shadowOffset
            )
        }
        shapeRenderer.color = gridColor
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
        val projection = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
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
        val mott = mTransform[mouseOnTile] ?: return false

        mouseCoord = currentCoord
        if (currentCoord == null) {
            mott.visible = false
            return false
        }
        mott.visible = true
        mott.position = mTransform[this[currentCoord]].position
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
            val gt = mTransform[sTag.getEntityId("GridRenderer")]
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

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            mTransform[mouseOnTile].visible = false
        }
    }

    override fun dispose() {
        clean()
        super.dispose()
    }

    private fun clean() {
        columns = 0
        rows = 0
        mouseOnTile = -1
        sTag.unregister(TAG_GRID_RENDERER)
    }

    private var time = 0f
    override fun processSystem() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            tileShaderComponent.shader = ShaderProgram(Scout["assets/shaders/vertex.glsl"], Scout["assets/shaders/fragment.glsl"])
            waterTileShaderComponent.shader = ShaderProgram(Scout["assets/shaders/vertex.glsl"], Scout["assets/shaders/fragment.glsl"])
        }

        time += world.delta
        try {
            waterTileShaderComponent.shader.apply {
                begin()
                setUniformf("time", time)
                end()
            }
        } catch (e: Exception) {
        }
    }
}
