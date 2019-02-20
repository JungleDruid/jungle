package net.natruid.jungle.views

import com.github.czyzby.lml.annotation.LmlActor
import com.kotcrab.vis.ui.widget.VisLabel
import net.natruid.jungle.core.Jungle

class DebugView : AbstractView() {
    companion object {
        var show = false
    }

    private var fps = 0
    private var timer = 0f

    @LmlActor("fpsLabel")
    lateinit var fpsLabel: VisLabel
    @LmlActor("ramLabel")
    lateinit var ramLabel: VisLabel
    @LmlActor("renderCallsLabel")
    lateinit var rcLabel: VisLabel

    override fun getViewId(): String {
        return "debug"
    }

    override fun render(delta: Float) {
        if (!show) return
        val renderer = Jungle.instance.renderer
        val batch = renderer.batch

        fps += 1
        timer += delta
        if (timer >= 1f) {
            timer -= 1f
            fpsLabel.setText("FPS: $fps")
            val runtime = Runtime.getRuntime()
            ramLabel.setText("RAM: ${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024}")
            rcLabel.setText("RC: ${batch.totalRenderCalls / fps}/${renderer.batchDraws / fps} [${renderer.batchBegins / fps}]")
            fps = 0
            batch.totalRenderCalls = 0
            renderer.batchDraws = 0
            renderer.batchBegins = 0
        }

        super.render(delta)
    }
}
