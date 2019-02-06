package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import ktx.math.times
import ktx.math.vec2
import net.natruid.jungle.core.Jungle
import kotlin.math.max
import kotlin.math.min

class CameraMovementSystem : BaseSystem(), InputProcessor {
    private val camera = Jungle.instance.camera
    private val speed = 512f
    private val maxZoom = 2f
    private val minZoom = 0.25f
    private val zoomStep = minZoom

    private val velocity = vec2(0f, 0f)

    private var zoom
        get() = camera.zoom
        set(value) {
            camera.zoom = max(minZoom, min(maxZoom, value))
            camera.update()
        }

    override fun processSystem() {
        if (!velocity.isZero) {
            camera.translate(velocity * speed * world.delta * zoom)
            camera.update()
            if (Jungle.instance.mouseMoved) Jungle.instance.mouseMoved(Gdx.input.x, Gdx.input.y)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.W) {
            velocity.y += 1
        }
        if (keycode == Input.Keys.S) {
            velocity.y -= 1
        }
        if (keycode == Input.Keys.A) {
            velocity.x -= 1
        }
        if (keycode == Input.Keys.D) {
            velocity.x += 1
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.W) {
            velocity.y -= 1
        }
        if (keycode == Input.Keys.S) {
            velocity.y += 1
        }
        if (keycode == Input.Keys.A) {
            velocity.x += 1
        }
        if (keycode == Input.Keys.D) {
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

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
}
