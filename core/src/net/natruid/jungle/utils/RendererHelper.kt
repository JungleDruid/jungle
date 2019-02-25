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

    private var rendererType = RendererType.NONE
    private var shapeType = ShapeRenderer.ShapeType.Line
    private var camera: OrthographicCamera? = null
    private val projection = Vector3()
    private var crop: Rectangle? = null

    fun begin(
        camera: OrthographicCamera,
        rendererType: RendererType,
        shapeType: ShapeRenderer.ShapeType = ShapeRenderer.ShapeType.Line,
        shaderProgram: ShaderProgram = Shader.defaultShaderProgram,
        crop: Rectangle? = null
    ) {
        if (rendererType == RendererType.SPRITE_BATCH) batchDraws += 1
        if (rendererType == RendererType.SPRITE_BATCH && batch.shader != shaderProgram) batch.shader = shaderProgram
        if (this.rendererType == rendererType) {
            if (this.camera == camera && (this.crop == crop || this.crop?.equals(crop) == true)) {
                if (rendererType != RendererType.SHAPE_RENDERER) return
                if (this.shapeType == shapeType) return
            }
        }

        end()

        when (rendererType) {
            RendererType.SPRITE_BATCH -> {
                if (crop != null) {
                    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
                    projection.set(crop.x, crop.y, 0f)
                    camera.project(projection)
                    Gdx.gl.glScissor(
                        projection.x.toInt(),
                        projection.y.toInt(),
                        (crop.width / camera.zoom).toInt(),
                        (crop.height / camera.zoom).toInt()
                    )
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

        this.rendererType = rendererType
        this.camera = camera
        this.crop = crop
    }

    fun end() {
        when (rendererType) {
            RendererType.SPRITE_BATCH -> {
                batch.end()
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                batch.color = Color.WHITE
                if (crop != null) {
                    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
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

        rendererType = RendererType.NONE
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }
}
