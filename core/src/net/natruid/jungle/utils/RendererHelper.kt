package net.natruid.jungle.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable

class RendererHelper : Disposable {

    val batch = SpriteBatch(1000, Shader.defaultShaderProgram)
    val shapeRenderer = ShapeRenderer()

    var batchDraws = 0
    var batchBegins = 0

    private var current = RendererType.NONE
    private var shapeType = ShapeRenderer.ShapeType.Line
    private val projection = Vector3()
    val cropRect = Rectangle()
    private var cropping = false
    private var cropped = false

    fun begin(
        camera: OrthographicCamera,
        rendererType: RendererType,
        shapeType: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Line,
        shaderProgram: ShaderProgram = Shader.defaultShaderProgram
    ) {
        if (rendererType == RendererType.SPRITE_BATCH) batchDraws += 1
        if (rendererType == RendererType.SPRITE_BATCH && batch.shader != shaderProgram) batch.shader = shaderProgram
        if (current == rendererType && (rendererType != RendererType.SHAPE_RENDERER || this.shapeType == shapeType)) return

        end()

        when (rendererType) {
            RendererType.SPRITE_BATCH -> {
                if (cropping) {
                    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
                    projection.set(cropRect.x, cropRect.y, 0f)
                    camera.project(projection)
                    Gdx.gl.glScissor(
                        projection.x.toInt(),
                        projection.y.toInt(),
                        (cropRect.width / camera.zoom).toInt(),
                        (cropRect.height / camera.zoom).toInt()
                    )
                    cropped = true
                }
                batchBegins += 1
                batch.color = Color.WHITE
                batch.projectionMatrix = camera.combined
                batch.enableBlending()
                batch.begin()
            }
            RendererType.SHAPE_RENDERER -> {
                shapeRenderer.projectionMatrix = camera.combined
                Gdx.gl.apply {
                    glEnable(GL20.GL_BLEND)
                    glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                }
                shapeRenderer.begin(shapeType)
                this.shapeType = shapeType
            }
            else -> {
                return
            }
        }

        current = rendererType
    }

    fun end() {
        when (current) {
            RendererType.SPRITE_BATCH -> {
                batch.end()
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                batch.color = Color.WHITE
                if (cropped) {
                    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
                    cropped = false
                }
                if (batch.shader != Shader.defaultShaderProgram) batch.shader = Shader.defaultShaderProgram
            }
            RendererType.SHAPE_RENDERER -> {
                shapeRenderer.end()
                Gdx.gl.glDisable(GL20.GL_BLEND)
            }
            else -> {
                return
            }
        }

        current = RendererType.NONE
    }

    fun crop(x: Float, y: Float, width: Float, height: Float) {
        cropRect.set(x, y, width, height)
        cropping = true
    }

    fun cancelCrop() {
        cropping = false
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }
}
