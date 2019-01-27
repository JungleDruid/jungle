package net.natruid.jungle.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import ktx.math.times
import ktx.math.vec2
import net.natruid.jungle.core.Jungle
import kotlin.math.max
import kotlin.math.min

class CameraMovementSystem : EntitySystem(0), InputProcessor {
    private val camera = Jungle.instance.camera
    private val speed = 512f
    private val maxZoom = 2f
    private val minZoom = 0.25f
    private val zoomStep = minZoom

    private var initialized = false
    private var velocity = vec2(0f, 0f)

    private var zoom
        get() = camera.zoom
        set(value) {
            camera.zoom = max(minZoom, min(maxZoom, value))
            camera.update()
        }

    private fun initialize() {
        initialized = true
        Jungle.instance.addInputProcessor(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)

        Jungle.instance.removeInputProcessor(this)
    }

    override fun update(deltaTime: Float) {
        if (!initialized) {
            initialize()
        }

        if (!velocity.isZero) {
            camera.translate(velocity * speed * deltaTime * zoom)
            camera.update()
        }

        super.update(deltaTime)
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