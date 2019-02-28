package net.natruid.jungle.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.async.skipFrame

class LoadingScreen : Screen {

    var done = false
        private set

    private val shapeRenderer = ShapeRenderer()

    private var maxProgress = 1
    private var progress = 0
    private var fakeProgress = 0f

    fun init(maxProgress: Int) {
        this.maxProgress = maxProgress
        progress = 0
        done = false
    }

    suspend fun progress() {
        progress += 1
        fakeProgress = 0f
        skipFrame()
    }

    suspend fun finish() {
        progress = maxProgress
        fakeProgress = 0f
        skipFrame()
    }

    override fun render(delta: Float) {
        if (done) return
        if (progress < maxProgress) {
            fakeProgress = (fakeProgress + 1f * delta).coerceAtMost(0.9f)
        }
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rect(100f, 100f, Gdx.graphics.width - 200f, 20f)
        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.rect(100f, 100f, Gdx.graphics.width - 200f, 20f)
        shapeRenderer.end()
        shapeRenderer.color = Color.GREEN
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rect(100f, 100f, (Gdx.graphics.width - 200f) * (progress + fakeProgress) / maxProgress, 20f)
        shapeRenderer.end()
        if (progress >= maxProgress) {
            done = true
        }
    }

    override fun hide() {}

    override fun show() {}

    override fun pause() {}

    override fun resume() {}

    override fun resize(width: Int, height: Int) {}

    override fun dispose() {}
}
