package net.natruid.jungle.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.graphics.use
import net.natruid.jungle.core.Jungle
import kotlin.math.floor

class GridRenderSystem(private val camera: OrthographicCamera) : EntitySystem() {
    private val gridSize = 64f

    private val batch = Jungle.instance.batch
    private val renderer = ShapeRenderer()
    private val color = Color(0.5f, 0.7f, 0.3f, 0.8f)

    var show = false

    init {
        renderer.color = color
        renderer.setAutoShapeType(true)
    }

    override fun update(deltaTime: Float) {
        if (!show) return

        super.update(deltaTime)

        val pos = camera.position
        val width = Gdx.graphics.width * camera.zoom
        val height = Gdx.graphics.height * camera.zoom

        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val halfGrid = gridSize / 2f
        val left = -halfWidth + pos.x - halfGrid
        val right = halfWidth + pos.x + halfGrid
        val bottom = -halfHeight + pos.y - halfGrid
        val top = halfHeight + pos.y + halfGrid

        val rows = (floor(height / gridSize)).toInt() / 2
        val columns = (floor(width / gridSize)).toInt() / 2

        val offsetX = gridSize - pos.x.rem(gridSize)
        val offsetY = gridSize - pos.y.rem(gridSize)

        batch.use {
            renderer.projectionMatrix = camera.combined
            renderer.begin()
            for (i in 0..rows + 1) {
                renderer.line(
                        left,
                        pos.y + i * gridSize + offsetY + halfGrid,
                        right,
                        pos.y + i * gridSize + offsetY + halfGrid
                )
                renderer.line(
                        left,
                        pos.y - i * gridSize + offsetY - halfGrid,
                        right,
                        pos.y - i * gridSize + offsetY - halfGrid
                )
            }
            for (i in 0..columns + 1) {
                renderer.line(
                        pos.x + i * gridSize + offsetX + halfGrid,
                        bottom,
                        pos.x + i * gridSize + offsetX + halfGrid,
                        top
                )
                renderer.line(
                        pos.x - i * gridSize + offsetX - halfGrid,
                        bottom,
                        pos.x - i * gridSize + offsetX - halfGrid,
                        top
                )
            }
            renderer.end()
        }
    }
}