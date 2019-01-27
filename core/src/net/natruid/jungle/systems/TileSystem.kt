package net.natruid.jungle.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pools
import ktx.ashley.add
import ktx.ashley.allOf
import ktx.ashley.entity
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.Point
import kotlin.math.roundToInt

class TileSystem : EntitySystem(), InputProcessor {
    private val tileSize = 64
    private val halfTileSize = tileSize / 2

    private val family = allOf(TileComponent::class, TransformComponent::class).get()
    private var tiles: ImmutableArray<Entity>? = null
    private var mouseCoord: Point? = null
    private var mouseOnTile: Entity? = null

    var columns = 0
        private set
    val rows: Int
        get() {
            val t = tiles ?: return 0
            return t.size() / columns
        }

    operator fun get(x: Int, y: Int): Entity? {
        val t = tiles ?: return null
        val index = y * columns + x
        if (index < 0 || index >= t.size()) return null
        return t[index]
    }

    operator fun get(coord: Point?): Entity? {
        if (coord == null) return null
        return get(coord.x, coord.y)
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        Jungle.instance.addInputProcessor(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        Jungle.instance.removeInputProcessor(this)
    }

    fun create(columns: Int, rows: Int) {
        val e = engine ?: return
        this.columns = columns
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val isBlock = random() > 0.8 && x > 0 && y > 0
                e.add {
                    entity {
                        with<TileComponent> {
                            walkable = !isBlock
                        }
                        with<TransformComponent> {
                            position.x = x * tileSize.toFloat()
                            position.y = y * tileSize.toFloat()
                        }
                        with<RectComponent> {
                            width = tileSize.toFloat()
                            height = tileSize.toFloat()
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
        tiles = e.getEntitiesFor(family)
        e.add {
            mouseOnTile = entity {
                with<TransformComponent> {
                    visible = false
                }
                with<RectComponent> {
                    width = tileSize.toFloat()
                    height = tileSize.toFloat()
                    color = Color.YELLOW
                }
            }
        }
    }

    private fun screenToCoord(screenX: Int, screenY: Int): Point? {
        if (columns == 0) return null

        val camera = Jungle.instance.camera
        val pos = Pools.obtain(Vector3::class.java).set(screenX.toFloat(), screenY.toFloat(), 0f)
        camera.unproject(pos)
        val x = pos.x.roundToInt() + halfTileSize
        val y = pos.y.roundToInt() + halfTileSize
        Pools.free(pos)
        if (x < 0 || x > columns * tileSize || y < 0 || y > rows * tileSize) return null

        return Point(x / tileSize, y / tileSize)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!this.checkProcessing()) return false

        val pos = screenToCoord(screenX, screenY)
        if (pos == mouseCoord) return false
        val mouseOnTileTransform = mouseOnTile?.getComponent(TransformComponent::class.java) ?: return false

        mouseCoord = pos
        if (pos == null) {
            mouseOnTileTransform.visible = false
            return false
        }
        mouseOnTileTransform.visible = true
        val tile = get(mouseCoord)?.getComponent(TransformComponent::class.java) ?: return false
        mouseOnTileTransform.position.set(tile.position)
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }
}