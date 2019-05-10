package net.natruid.jungle.views

import ktx.vis.window
import net.natruid.jungle.core.Marsh

class TestView : AbstractView() {
    init {
        addActor(window(Marsh.I18N["assets/locale/UI"]["title"]) {
            label(Marsh.I18N["assets/locale/UI"]["test"])
            textField("Test")
            row()
            textField("Another Text Field")
            pack()
            centerWindow()
        })
    }
}
