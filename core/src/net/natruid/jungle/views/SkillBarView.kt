package net.natruid.jungle.views

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.github.czyzby.lml.annotation.LmlActor
import com.kotcrab.vis.ui.widget.VisTable
import net.natruid.jungle.core.Jungle

class SkillBarView : AbstractView() {
    @LmlActor("apBar")
    private lateinit var apBar: VisTable

    private val apCircleList = ArrayList<ApPointCircle>()

    override fun show() {
        super.show()
        repeat(6) {
            apCircleList.add(Pools.obtain(ApPointCircle::class.java))
        }
        apCircleList.forEach {
            apBar.add(it)
        }
        apBar.isVisible = false
    }

    override fun hide() {
        super.hide()
        apCircleList.forEach {
            apBar.removeActor(it)
            Pools.free(it)
        }
        apCircleList.clear()
    }

    fun setAp(value: Int, preview: Int = -1) {
        for (i in 0 until apCircleList.size) {
            apCircleList[i].color = when {
                preview >= 0 && i >= value - preview && i < value -> Color.RED
                preview >= 0 && i < value - preview || preview < 0 && i < value -> Color.GREEN
                else -> Color.GRAY
            }
        }
        apBar.isVisible = true
    }

    fun hideAp() {
        apBar.isVisible = false
    }

    override fun getViewId(): String {
        return "skill-bar"
    }

    class ApPointCircle : Widget(), Pool.Poolable {
        companion object {
            private val shapeRenderer by lazy { ShapeRenderer() }
        }

        init {
            setSize(30f, 30f)
        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            batch.end()
            shapeRenderer.projectionMatrix = Jungle.instance.uiViewport.camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = color
            shapeRenderer.circle(x + width / 2, y + height / 2, 10f)
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.BLACK
            shapeRenderer.circle(x + width / 2, y + height / 2, 10f)
            shapeRenderer.end()
            batch.begin()
        }

        override fun getPrefWidth(): Float {
            return width
        }

        override fun getPrefHeight(): Float {
            return height
        }

        override fun reset() {
            setColor(1f, 1f, 1f, 1f)
        }
    }
}
