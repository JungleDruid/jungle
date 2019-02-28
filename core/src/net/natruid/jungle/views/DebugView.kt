package net.natruid.jungle.views

import com.github.czyzby.lml.annotation.LmlActor
import com.kotcrab.vis.ui.widget.VisLabel
import net.natruid.jungle.utils.RendererHelper

class DebugView : AbstractView() {
    companion object {
        var show = false
    }

    var renderer: RendererHelper? = null

    private var fps = 0
    private var timer = 0f
    private var threads = 0

    @LmlActor("fpsLabel")
    lateinit var fpsLabel: VisLabel
    @LmlActor("ramLabel")
    lateinit var ramLabel: VisLabel
    @LmlActor("renderCallsLabel")
    lateinit var rcLabel: VisLabel
    @LmlActor("threadsLabel")
    lateinit var threadsLabel: VisLabel
    @LmlActor("tileLabel")
    lateinit var tileLabel: VisLabel
    @LmlActor("unitLabel")
    lateinit var unitLabel: VisLabel

    override fun getViewId(): String {
        return "debug"
    }

    override fun render(delta: Float) {
        if (!show) return

        fps += 1
        timer += delta

        Thread.activeCount().let {
            if (it > threads) threads = it
        }
        if (timer >= 1f) {
            timer -= 1f
            fpsLabel.setText("FPS: $fps")
            val runtime = Runtime.getRuntime()
            ramLabel.setText("RAM: ${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024}")
            threadsLabel.setText("Threads: $threads")
            fps = 0
            threads = 0
        }
        renderer?.let {
            val batch = it.batch
            rcLabel.setText("RC: ${batch.totalRenderCalls}/${it.begins} (${it.diffs})")
            batch.totalRenderCalls = 0
            it.begins = 0
            it.diffs = 0
        }

        super.render(delta)
    }
}
