package net.natruid.jungle.views

import com.github.czyzby.lml.annotation.LmlActor
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisWindow

class UnitStatsQuickview : AbstractView() {
    @LmlActor("base")
    lateinit var base: VisWindow
    @LmlActor("hp")
    lateinit var hp: VisLabel
    @LmlActor("int")
    lateinit var int: VisLabel
    @LmlActor("det")
    lateinit var det: VisLabel
    @LmlActor("end")
    lateinit var end: VisLabel
    @LmlActor("awa")
    lateinit var awa: VisLabel
    @LmlActor("luc")
    lateinit var luc: VisLabel

    override fun getViewId(): String {
        return "unit-stats-quickview"
    }
}
