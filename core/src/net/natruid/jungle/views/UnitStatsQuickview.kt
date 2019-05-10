package net.natruid.jungle.views

import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.vis.window

class UnitStatsQuickview : AbstractView() {
    lateinit var hp: VisLabel
    lateinit var int: VisLabel
    lateinit var det: VisLabel
    lateinit var end: VisLabel
    lateinit var awa: VisLabel
    lateinit var luc: VisLabel
    val base: VisWindow = window("", "info") {
        isMovable = false
        defaults().pad(0f, 5f, 0f, 5f)
        label("HP", "green-small")
        hp = label("0", "green-small")
        row()
        label("I", "green-small")
        int = label("0", "green-small")
        row()
        label("D", "green-small")
        det = label("0", "green-small")
        row()
        label("E", "green-small")
        end = label("0", "green-small")
        row()
        label("A", "green-small")
        awa = label("0", "green-small")
        row()
        label("L", "green-small")
        luc = label("0", "green-small")
        pack()
        width = 128f
    }

    init {
        addActor(base)
    }
}
