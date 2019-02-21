package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import ktx.math.times
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.Point
import kotlin.math.max
import kotlin.math.min

class CameraMovementSystem : BaseSystem(), InputProcessor {
    private val camera = Jungle.instance.camera
    private val cropRect = Jungle.instance.renderer.cropRect
    private val speed = 512f
    private val maxZoom = 2f
    private val minZoom = 0.25f
    private val zoomStep = minZoom

    private val velocity = Vector2(0f, 0f)

    private var zoom
        get() = camera.zoom
        set(value) {
            val oldZoom = camera.zoom
            camera.zoom = max(minZoom, min(maxZoom, value))
            val diff = camera.zoom - oldZoom
            if (diff != 0f) {
                val x = (Gdx.input.x - Gdx.graphics.width / 2) * -diff
                val y = (Gdx.input.y - Gdx.graphics.height / 2) * diff
                camera.translate(x, y)
                clamp()
                camera.update()
            }
        }

    private fun clamp() {
        camera.position.apply {
            x = x.coerceIn(cropRect.x, cropRect.x + cropRect.width)
            y = y.coerceIn(cropRect.y, cropRect.y + cropRect.height)
        }
    }

    override fun processSystem() {
        if (!velocity.isZero) {
            camera.translate(velocity * speed * world.delta * zoom)
            clamp()
            camera.update()
            if (Jungle.instance.mouseMoved) Jungle.instance.mouseMoved(Gdx.input.x, Gdx.input.y)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            velocity.y += 1
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            velocity.y -= 1
        }
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            velocity.x -= 1
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            velocity.x += 1
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            velocity.y -= 1
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            velocity.y += 1
        }
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            velocity.x += 1
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            velocity.x -= 1
        }

        if (keycode == Input.Keys.EQUALS) {
            zoom -= zoomStep
        }
        if (keycode == Input.Keys.MINUS) {
            zoom += zoomStep
        }

        return false
    }

    override fun scrolled(amount: Int): Boolean {
        zoom += amount * zoomStep
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    private val dragPoint = Point()
    private var dragStarted = false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragStarted) return false
        camera.translate((dragPoint.x - screenX) * camera.zoom, (screenY - dragPoint.y) * camera.zoom)
        camera.update()
        dragPoint.set(screenX, screenY)
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        dragPoint.set(screenX, screenY)
        dragStarted = true
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        dragStarted = false
        return false
    }
}
