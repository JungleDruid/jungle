package net.natruid.jungle.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.vis.util.VisLml
import com.kotcrab.vis.ui.VisUI
import net.natruid.jungle.screens.AbstractScreen
import net.natruid.jungle.screens.TestScreen
import net.natruid.jungle.utils.Bark
import net.natruid.jungle.utils.DesktopClient
import net.natruid.jungle.views.TestView
import java.lang.management.ManagementFactory

class Jungle(private val client: DesktopClient?) : ApplicationListener, InputProcessor {
    var batch: SpriteBatch? = null
        private set

    private var currentScreen: AbstractScreen? = null
    private var currentView: AbstractLmlView? = null

    init {
        instance = this
    }

    override fun create() {
        Marsh.load()

        batch = SpriteBatch()
        Gdx.input.inputProcessor = this

        VisUI.load(Bark("assets/ui/jungle.json"))

        val bundle = Marsh.I18N["assets/locale/UI"]
        client?.setTitle(bundle["title"])

        currentView = TestView()
        createParser().createView(currentView, currentView?.templateFile)

        setScreen(TestScreen())
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val delta = Gdx.graphics.deltaTime
        currentScreen?.render(delta)
        currentView?.render(delta)
    }

    override fun dispose() {
        currentScreen?.dispose()
        batch?.dispose()
        VisUI.dispose()
    }

    private fun setScreen(screen: AbstractScreen) {
        currentScreen?.dispose()

        currentScreen = screen
    }

    override fun pause() {
        currentScreen?.pause()
    }

    override fun resume() {
        currentScreen?.resume()
    }

    override fun resize(width: Int, height: Int) {
        currentScreen?.resize(width, height)
        currentView?.resize(width, height, true)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return currentView?.stage?.touchUp(screenX, screenY, pointer, button) == true
                || currentScreen?.touchUp(screenX, screenY, pointer, button) == true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return currentView?.stage?.mouseMoved(screenX, screenY) == true
                || currentScreen?.mouseMoved(screenX, screenY) == true
    }

    override fun keyTyped(character: Char): Boolean {
        return currentView?.stage?.keyTyped(character) == true
                || currentScreen?.keyTyped(character) == true
    }

    override fun scrolled(amount: Int): Boolean {
        return currentView?.stage?.scrolled(amount) == true
                || currentScreen?.scrolled(amount) == true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (currentView?.stage?.keyUp(keycode) == true || currentScreen?.keyUp(keycode) == true) {
            return true
        }

        if (debug && keycode == Input.Keys.R) {
            setScreen(TestScreen())
            if (currentView != null) {
                currentView?.stage?.clear()
                createParser().createView(currentView, currentView!!.templateFile)
            }

            client?.resize(1024, 768)

            return true
        }

        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return currentView?.stage?.touchDragged(screenX, screenY, pointer) == true
                || currentScreen?.touchDragged(screenX, screenY, pointer) == true
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.ESCAPE) {
            unfocusAll()
        }

        return currentView?.stage?.keyDown(keycode) == true
                || currentScreen?.keyDown(keycode) == true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (currentView?.stage?.touchDown(screenX, screenY, pointer, button) == true) {
            return true
        }

        unfocusAll()

        return currentScreen?.touchDown(screenX, screenY, pointer, button) == true
    }

    fun unfocusAll() {
        currentView?.stage?.unfocusAll()
        currentScreen?.stage?.unfocusAll()
    }

    companion object {
        val debug = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0

        var instance: Jungle? = null
            private set

        fun createParser(): LmlParser {
            return VisLml.parser().i18nBundle(Marsh.I18N["assets/locale/UI"]).build()
        }
    }
}
