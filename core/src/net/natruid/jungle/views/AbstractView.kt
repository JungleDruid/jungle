package net.natruid.jungle.views

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import net.natruid.jungle.core.Jungle

abstract class AbstractView : AbstractLmlView(Stage(ScreenViewport(), Jungle.instance!!.batch!!)) {
    abstract override fun getTemplateFile(): FileHandle
}
