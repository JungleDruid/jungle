package net.natruid.jungle.screens

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.graphics.use
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.CameraMovementSystem

class FieldScreen : AbstractScreen() {
    private val renderer = ShapeRenderer()

    init {
        engine.addSystem(CameraMovementSystem(camera))
    }

    override fun render(delta: Float) {
        val gridSize = 64f
        val rows = 11
        val columns = 12

        super.render(delta)
        val batch = Jungle.instance.batch
        batch.use {
            renderer.setAutoShapeType(true)
            renderer.projectionMatrix = camera.combined
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
}