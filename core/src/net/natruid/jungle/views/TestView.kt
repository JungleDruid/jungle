package net.natruid.jungle.views

import com.badlogic.gdx.files.FileHandle
import net.natruid.jungle.utils.Scout

class TestView : AbstractView() {
    override fun getViewId(): String {
        return "test"
    }

    override fun getTemplateFile(): FileHandle {
        return Scout["assets/templates/test.lml"]
    }
}
