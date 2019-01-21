package net.natruid.jungle.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle

class TestView : AbstractView() {
    override fun getViewId(): String {
        return "test"
    }

    override fun getTemplateFile(): FileHandle {
        return Gdx.files.local("templates/test.lml")
    }
}