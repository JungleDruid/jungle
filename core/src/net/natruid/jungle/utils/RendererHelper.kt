package net.natruid.jungle.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable

class RendererHelper : Disposable {

    val batch = SpriteBatch(1000, Shader.getDefaultShaderProgram())
    val shapeRenderer = ShapeRenderer()

    var begins = 0
    var diffs = 0

    private var rendererType = RendererType.NONE
    private var camera: OrthographicCamera? = null
    private val projection = Vector3()
    private var crop: Rectangle? = null

    fun begin(
        camera: OrthographicCamera,
        rendererType: RendererType,
        crop: Rectangle? = null
    ) {
        assert(rendererType != RendererType.NONE)

        begins += 1
        val same = this.rendererType == rendererType

        if (!same) {
            end(false)
        }

        var needFlush = true

        if (!same || this.camera != camera) {
            needFlush = false
            if (rendererType == RendererType.SPRITE_BATCH)
                batch.projectionMatrix = camera.combined
            else {
                if (same) {
                    shapeRenderer.flush()
                    batch.totalRenderCalls += 1
                }
                shapeRenderer.projectionMatrix = camera.combined
            }
        }

        if (crop != null) {
            if (crop != this.crop || this.camera != camera) {
                if (needFlush && same) {
                    if (rendererType == RendererType.SPRITE_BATCH) {
                        batch.flush()
                    } else {
                        shapeRenderer.flush()
                        batch.totalRenderCalls += 1
                    }
                }
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
        } else if (this.crop != null) {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
        }

        this.crop = crop
        this.camera = camera
        this.rendererType = rendererType

        if (same) return

        diffs += 1

        when (rendererType) {
            RendererType.SPRITE_BATCH -> {
                batch.enableBlending()
                batch.begin()
            }
            else -> {
                Gdx.gl.apply {
                    glEnable(GL20.GL_BLEND)
                    glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                }
                shapeRenderer.begin(when (rendererType) {
                    RendererType.SHAPE_POINT -> ShapeType.Point
                    RendererType.SHAPE_LINE -> ShapeType.Line
                    RendererType.SHAPE_FILLED -> ShapeType.Filled
                    else -> error("No such shape type: $rendererType")
                })
            }
        }
    }

    fun end(endCrop: Boolean = true) {
        when (rendererType) {
            RendererType.NONE -> {
                return
            }
            RendererType.SPRITE_BATCH -> {
                batch.end()
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                batch.color = Color.WHITE
                if (batch.shader != Shader.getDefaultShaderProgram()) batch.shader = Shader.getDefaultShaderProgram()
            }
            else -> {
                shapeRenderer.end()
                Gdx.gl.glDisable(GL20.GL_BLEND)
                batch.totalRenderCalls += 1
            }
        }

        if (endCrop && crop != null) {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
        }

        rendererType = RendererType.NONE
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }
}
