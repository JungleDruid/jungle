package net.natruid.jungle.views

import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisLabel
import ktx.vis.table
import net.natruid.jungle.utils.RendererHelper

class DebugView : AbstractView() {
    companion object {
        var show = false
    }

    var renderer: RendererHelper? = null

    private var fps = 0
    private var timer = 0f
    private var threads = 0

    private lateinit var fpsLabel: VisLabel
    private lateinit var ramLabel: VisLabel
    private lateinit var rcLabel: VisLabel
    private lateinit var threadsLabel: VisLabel
    lateinit var tileLabel: VisLabel
        private set
    lateinit var unitLabel: VisLabel
        private set

    init {
        addActor(table {
            setFillParent(true)
            align(Align.topLeft)
            padLeft(5f)
            defaults().align(Align.left)
            fpsLabel = label("FPS: 0", "green-small")
            row()
            ramLabel = label("RAM: 0", "green-small")
            row()
            rcLabel = label("RC: 0", "green-small")
            row()
            threadsLabel = label("Threads: 0", "green-small")
            row()
            tileLabel = label("Tile: -1", "green-small")
            row()
            unitLabel = label("Unit: -1", "green-small")
        })
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
