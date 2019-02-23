package net.natruid.jungle.systems

import com.artemis.Aspect
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import ktx.graphics.use
import net.natruid.jungle.components.*
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.*
import net.natruid.jungle.utils.Constants.DOWN
import net.natruid.jungle.utils.Constants.LEFT
import net.natruid.jungle.utils.Constants.RIGHT
import net.natruid.jungle.utils.Constants.UP
import net.natruid.jungle.utils.Constants.Z_MOUSE_ON_TILE
import net.natruid.jungle.utils.Constants.Z_OBSTACLE
import net.natruid.jungle.utils.extensions.forEach
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

class TileSystem : BaseSystem(), InputProcessor {
    companion object {
        const val tileSize = 64
        private const val TAG_GRID_RENDERER = "GridRenderer"
        private const val TAG_MOUSE_ON_TILE = "MouseOnTile"
    }

    private var columns = 0
    private var rows = 0
    var mouseCoord: Point? = null
    var seed = 0L
        private set

    private val halfTileSize = tileSize / 2

    private lateinit var mTile: ComponentMapper<TileComponent>
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mTexture: ComponentMapper<TextureComponent>
    private lateinit var mRect: ComponentMapper<RectComponent>
    private lateinit var mRenderable: ComponentMapper<RenderableComponent>
    private lateinit var mObstacle: ComponentMapper<ObstacleComponent>
    private lateinit var mShader: ComponentMapper<ShaderComponent>
    private lateinit var tileEntities: Array<IntArray>
    private lateinit var tagManager: TagManager
    private var mouseOnTile
        get() = tagManager.getEntityId(TAG_MOUSE_ON_TILE)
        set(value) {
            if (value >= 0) tagManager.register(TAG_MOUSE_ON_TILE, value)
            else tagManager.unregister(TAG_MOUSE_ON_TILE)
        }
    private val renderer = Jungle.instance.renderer
    private val shapeRenderer = renderer.shapeRenderer
    private val camera = Jungle.instance.camera
    private val gridColor = Color(0.5f, 0.7f, 0.3f, 0.8f)
    private val gridRenderCallback: ((TransformComponent) -> Unit) = { renderGrid() }
    private val mouseOnTileColor = Color(1f, 1f, 0f, 0.4f)
    private val tileShaderComponent = ShaderComponent().apply {
        shader = Shader(fragment = "tile")
        shader.getInstance().use {
            it.setUniformf("bound", 1f - 64f / 96f)
        }
        blendSrcFunc = GL20.GL_SRC_ALPHA
        blendDstFunc = GL20.GL_ONE
    }
    private val waterTileShaderComponent = ShaderComponent().apply {
        shader = Shader(fragment = "tile")
        shader.getInstance().use {
            it.setUniformf("bound", 1f - 64f / 96f)
        }
        blendSrcFunc = GL20.GL_SRC_ALPHA
        blendDstFunc = GL20.GL_ONE
    }
    private val stoneInWaterShaderComponent = ShaderComponent().apply {
        shader = Shader(fragment = "tile")
        shader.getInstance().use {
            it.setUniformf("bound", 0.45f)
        }
    }

    operator fun get(coord: Point): Int {
        if (!isCoordValid(coord)) return -1
        return tileEntities[coord.x][coord.y]
    }

    operator fun get(x: Int, y: Int): Int {
        if (!isCoordValid(x, y)) return -1
        return tileEntities[x][y]
    }

    fun isCoordValid(coord: Point?): Boolean {
        if (coord == null) return false
        return isCoordValid(coord.x, coord.y)
    }

    fun isCoordValid(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < columns && y < rows
    }

    fun neighbors(entityId: Int, diagonal: Boolean = false): IntArray {
        return neighbors(mTile[entityId].coord, diagonal)
    }

    fun neighbors(coord: Point, diagonal: Boolean = false): IntArray {
        return IntArray(4) { this[coord.neighbor(it, diagonal)] }
    }

    fun up(coord: Point): Int {
        val x = coord.x
        val y = coord.y + 1
        return this[x, y]
    }

    fun down(coord: Point): Int {
        val x = coord.x
        val y = coord.y - 1
        return this[x, y]
    }

    fun right(coord: Point): Int {
        val x = coord.x + 1
        val y = coord.y
        return this[x, y]
    }

    fun left(coord: Point): Int {
        val x = coord.x - 1
        val y = coord.y
        return this[x, y]
    }

    private val dirtTexture = TextureRegion(Texture(Scout["assets/img/tiles/dirt.png"]))
    private val grassTexture = TextureRegion(Texture(Scout["assets/img/tiles/grass.png"]))
    private val roadTexture = TextureRegion(Texture(Scout["assets/img/tiles/road.png"]))
    private val roadTextureUpDown = TextureRegion(Texture(Scout["assets/img/tiles/road-ud.png"]))
    private val roadTextureRightUp = TextureRegion(Texture(Scout["assets/img/tiles/road-ru.png"]))
    private val roadTextureRightUpLeft = TextureRegion(Texture(Scout["assets/img/tiles/road-rul.png"]))
    private val roadTextureRightUpLeftDown = TextureRegion(Texture(Scout["assets/img/tiles/road-ruld.png"]))
    private val waterTexture = TextureRegion(Texture(Scout["assets/img/tiles/water.png"]))
    private val bridgeTexture = TextureRegion(Texture(Scout["assets/img/tiles/bridge.png"]))
    private val bridgeMultiDirectionTexture = TextureRegion(Texture(Scout["assets/img/tiles/bridge-multi-direction.png"]))
    private val treeTexture = TextureRegion(Texture(Scout["assets/img/tiles/tree.png"]))
    private val rockTexture = TextureRegion(Texture(Scout["assets/img/tiles/rock.png"]))

    fun create(columns: Int, rows: Int, seed: Long = Random.nextLong()) {
        reset()
        this.columns = columns
        this.rows = rows
        this.seed = seed
        val generator = MapGenerator(columns, rows, world, seed)
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
                        TerrainType.WATER -> waterTexture
                    }
                    color = when (cTile.terrainType) {
                        TerrainType.WATER ->
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
                when {
                    cTile.terrainType == TerrainType.WATER -> world.edit(tile).add(waterTileShaderComponent)
                    else -> world.edit(tile).add(tileShaderComponent)
                }
                if (cTile.hasRoad) {
                    // create road or bridge
                    world.create().let { road ->
                        mTransform.create(road).apply {
                            mTransform[tile].let {
                                position = it.position
                                z = it.z + 0.1f
                            }
                            val coord = cTile.coord
                            var roadCount = 0
                            val dirs = Array(4) { i ->
                                val t = when (i) {
                                    0 -> right(coord)
                                    1 -> up(coord)
                                    2 -> left(coord)
                                    else -> down(coord)
                                }
                                when {
                                    t < 0 -> false
                                    mTile[t].hasRoad -> {
                                        roadCount += 1
                                        true
                                    }
                                    else -> false
                                }
                            }
                            if (cTile.terrainType == TerrainType.WATER) {
                                // create bridge
                                var rotate = false
                                var multiDirection = false
                                when (roadCount) {
                                    1 -> {
                                        if (dirs[1] || dirs[3])
                                            rotate = true
                                    }
                                    2 -> {
                                        if (dirs[1] && dirs[3]) {
                                            rotate = true
                                        } else if (!(dirs[0] && dirs[2])) {
                                            multiDirection = true
                                        }
                                    }
                                    else -> {
                                        multiDirection = true
                                    }
                                }
                                if (rotate) rotation = 90f
                                mTexture.create(road).region = when {
                                    multiDirection -> bridgeMultiDirectionTexture
                                    else -> {
                                        z += 0.01f
                                        bridgeTexture
                                    }
                                }
                            } else {
                                // create road
                                var directions = 0
                                for ((i, dir) in dirs.withIndex()) {
                                    if (dir) directions = directions.or(1.shl(i))
                                }
                                mTexture.create(road).region = when (directions) {
                                    UP.or(DOWN), UP, DOWN -> {
                                        roadTextureUpDown
                                    }
                                    LEFT.or(RIGHT), LEFT, RIGHT -> {
                                        rotation = 90f
                                        roadTextureUpDown
                                    }
                                    RIGHT.or(UP) -> {
                                        roadTextureRightUp
                                    }
                                    UP.or(LEFT) -> {
                                        rotation = 90f
                                        roadTextureRightUp
                                    }
                                    LEFT.or(DOWN) -> {
                                        rotation = 180f
                                        roadTextureRightUp
                                    }
                                    DOWN.or(RIGHT) -> {
                                        rotation = 270f
                                        roadTextureRightUp
                                    }
                                    RIGHT.or(UP).or(LEFT) -> {
                                        roadTextureRightUpLeft
                                    }
                                    UP.or(LEFT).or(DOWN) -> {
                                        rotation = 90f
                                        roadTextureRightUpLeft
                                    }
                                    LEFT.or(DOWN).or(RIGHT) -> {
                                        rotation = 180f
                                        roadTextureRightUpLeft
                                    }
                                    DOWN.or(RIGHT).or(UP) -> {
                                        rotation = 270f
                                        roadTextureRightUpLeft
                                    }
                                    RIGHT.or(UP).or(LEFT).or(DOWN) -> {
                                        roadTextureRightUpLeftDown
                                    }
                                    else -> roadTexture
                                }
                            }
                        }
                    }
                }

                if (cTile.obstacle >= 0) {
                    val obstacle = cTile.obstacle
                    val cObstacle = mObstacle[obstacle]
                    mTransform.create(obstacle).apply {
                        position = mTransform[tile].position
                        z = Z_OBSTACLE
                        scale.set(
                            generator.random.nextFloat() * 0.2f + 0.9f,
                            generator.random.nextFloat() * 0.2f + 0.9f
                        )
                    }
                    mTexture.create(obstacle).apply {
                        region = when (cObstacle.type) {
                            ObstacleType.TREE -> treeTexture
                            ObstacleType.ROCK -> {
                                if (cTile.terrainType == TerrainType.WATER) {
                                    world.edit(obstacle).add(stoneInWaterShaderComponent)
                                }
                                rockTexture
                            }
                            else -> treeTexture
                        }
                        flipX = generator.random.nextBoolean()
                    }
                }
            }
        }
        world.create().let { entityId ->
            tagManager.register("GridRenderer", entityId)
            val visible = mTransform[entityId]?.visible ?: false
            mTransform.create(entityId).visible = visible
            mRenderable.create(entityId).renderCallback = gridRenderCallback
        }
        world.create().let { entityId ->
            mouseOnTile = entityId
            mTransform.create(entityId).apply {
                visible = false
                z = Z_MOUSE_ON_TILE
            }
            mRect.create(entityId).apply {
                width = tileSize.toFloat()
                height = tileSize.toFloat()
                type = ShapeRenderer.ShapeType.Filled
                color = mouseOnTileColor
            }
        }
        renderer.crop(
            -halfTileSize.toFloat(),
            -halfTileSize.toFloat(),
            (columns * tileSize).toFloat(),
            (rows * tileSize).toFloat()
        )
    }

    fun getDistance(tile1: Int, tile2: Int): Float {
        val coord1 = mTile[tile1].coord
        val coord2 = mTile[tile2].coord
        val x = abs(coord1.x - coord2.x)
        val y = abs(coord1.y - coord2.y)
        return min(x, y) * 1.5f + abs(x - y)
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
            val gt = mTransform[tagManager.getEntityId("GridRenderer")]
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
        reset()
        super.dispose()
    }

    private fun reset() {
        columns = 0
        rows = 0
        mouseOnTile = -1
        tagManager.unregister(TAG_GRID_RENDERER)
        renderer.cancelCrop()
    }

    override fun processSystem() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            world.aspectSubscriptionManager.get(Aspect.all(ShaderComponent::class.java)).entities.forEach {
                mShader[it].shader.reset()
            }
        }

        try {
            waterTileShaderComponent.shader.getInstance().use {
                it.setUniformf("time", Jungle.instance.time)
            }
        } catch (e: Exception) {
        }
    }
}
