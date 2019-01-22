package net.natruid.jungle.screens

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.graphics.use
import ktx.math.vec2
import net.natruid.jungle.core.Jungle

class FieldScreen : AbstractScreen() {
    private val renderer = ShapeRenderer()

    override fun render(delta: Float) {
        val gridSize = 64f
        val rows = 11
        val columns = 12

        renderSystem.camera.translate(cameraMovement)
        renderSystem.camera.update()
        super.render(delta)
        val batch = Jungle.instance.batch
        batch.use {
            renderer.setAutoShapeType(true)
            renderer.projectionMatrix = renderSystem.camera.combined
            renderer.begin()
            for (i in 0..rows) {
                renderer.line(
                        -columns / 2f * gridSize,
                        -rows / 2f * gridSize + i * gridSize,
                        columns / 2f * gridSize,
                        -rows / 2f * gridSize + i * gridSize
                )
            }
            for (i in 0..columns) {
                renderer.line(
                        -columns / 2f * gridSize + i * gridSize,
                        -rows / 2f * gridSize,
                        -columns / 2f * gridSize + i * gridSize,
                        rows / 2f * gridSize
                )
            }
            renderer.end()
        }
    }

    private val cameraMovement = vec2()

    private val cameraSpeed = 10f

    override fun keyDown(keycode: Int): Boolean {
        super.keyDown(keycode)

        if (keycode == Input.Keys.W) {
            cameraMovement.y += cameraSpeed
        }
        if (keycode == Input.Keys.S) {
            cameraMovement.y -= cameraSpeed
        }
        if (keycode == Input.Keys.A) {
            cameraMovement.x -= cameraSpeed
        }
        if (keycode == Input.Keys.D) {
            cameraMovement.x += cameraSpeed
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        super.keyUp(keycode)

        if (keycode == Input.Keys.W) {
            cameraMovement.y -= cameraSpeed
        }
        if (keycode == Input.Keys.S) {
            cameraMovement.y += cameraSpeed
        }
        if (keycode == Input.Keys.A) {
            cameraMovement.x += cameraSpeed
        }
        if (keycode == Input.Keys.D) {
            cameraMovement.x -= cameraSpeed
        }

        if (keycode == Input.Keys.EQUALS) {
            renderSystem.zoom -= 0.2f
        }
        if (keycode == Input.Keys.MINUS) {
            renderSystem.zoom += 0.2f
        }

        return false
    }

    override fun scrolled(amount: Int): Boolean {
        super.scrolled(amount)
        renderSystem.zoom += amount * 0.2f
        return false
    }
}