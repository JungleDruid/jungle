package net.natruid.jungle.views

import com.badlogic.gdx.files.FileHandle
import com.github.czyzby.lml.annotation.LmlActor
import com.kotcrab.vis.ui.widget.VisLabel
import net.natruid.jungle.utils.Scout

class DebugView : AbstractView() {
    companion object {
        var show = false
    }

    private var fps = 0
    private var timer = 0f

    @LmlActor("fpsLabel")
    val fpsLabel: VisLabel? = null

    override fun getTemplateFile(): FileHandle {
        return Scout["assets/templates/debug.lml"]
    }

    override fun getViewId(): String {
        return "debug"
    }

    override fun render(delta: Float) {
        if (!show) return

        fps += 1
        timer += delta
        if (timer >= 1f) {
            timer -= 1f
            fpsLabel?.setText(fps.toString())
            fps = 0
        }

        super.render(delta)
    }
}