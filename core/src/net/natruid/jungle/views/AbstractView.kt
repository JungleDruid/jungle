package net.natruid.jungle.views

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.Scout
import kotlin.reflect.KClass

abstract class AbstractView : AbstractLmlView(Stage(Jungle.instance.uiViewport)), InputProcessor {
    override fun getTemplateFile(): FileHandle {
        return Scout["assets/templates/$viewId.lml"]
    }

    companion object {
        inline fun <reified Type : AbstractView> createView() = createView(Type::class)

        fun <Type : AbstractView> createView(type: KClass<Type>): Type {
            val view = type.java.getDeclaredConstructor().newInstance()
            Jungle.lmlParser.createView(view, view.templateFile)
            return view
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage?.touchDown(screenX, screenY, pointer, button) ?: false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return stage?.touchDragged(screenX, screenY, pointer) ?: false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage?.touchUp(screenX, screenY, pointer, button) ?: false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return stage?.mouseMoved(screenX, screenY) ?: false
    }

    override fun keyDown(keycode: Int): Boolean {
        return stage?.keyDown(keycode) ?: false
    }

    override fun keyTyped(character: Char): Boolean {
        return stage?.keyTyped(character) ?: false
    }

    override fun keyUp(keycode: Int): Boolean {
        return stage?.keyUp(keycode) ?: false
    }

    override fun scrolled(amount: Int): Boolean {
        return stage?.scrolled(amount) ?: false
    }
}
