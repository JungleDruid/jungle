package net.natruid.jungle.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.vis.util.VisLml
import com.kotcrab.vis.ui.VisUI
import net.natruid.jungle.screens.AbstractScreen
import net.natruid.jungle.screens.FieldScreen
import net.natruid.jungle.screens.TestScreen
import net.natruid.jungle.utils.*
import net.natruid.jungle.views.AbstractView
import net.natruid.jungle.views.DebugView
import net.natruid.jungle.views.TestView
import java.lang.management.ManagementFactory

class Jungle(private val client: Client, debug: Boolean = false) : ApplicationListener, InputProcessor {
    val renderer by lazy { RendererHelper() }
    val camera by lazy { OrthographicCamera() }
    val uiViewport by lazy { ScreenViewport() }

    var mouseMoved = false
        private set

    private var currentScreen: AbstractScreen? = null
    private var currentView: AbstractLmlView? = null
    private val inputProcessors = ArrayList<InputProcessor>()
    private var debugView: DebugView? = null
    private var targetFPS = 60
    private var backgroundFPS = 10
    private var pauseOnBackground = false
    private var resizing = false
    private var vSync = true

    init {
        instance = this
        Companion.debug = Companion.debug || debug
    }

    override fun create() {
        Logger.stopwatch("Initialization") {
            Logger.debug { "Initializing..." }
            client.init()

            Logger.catch("Data loading failed.") {
                Marsh.load()
            }

            Gdx.input.inputProcessor = this

            Logger.catch("Skin loading failed.") {
                VisUI.load(Bark("assets/ui/jungle.json"))
            }

            Logger.catch("I18n bundle loading failed.") {
                val bundle = Marsh.I18N["assets/locale/UI"]
                client.setTitle(bundle["title"])
            }

            setScreen(FieldScreen())
            if (debug) {
                debugView = AbstractView.createView()
                DebugView.show = true
            }
            Gdx.graphics.setVSync(vSync)
            Logger.info { "Game initialized." }
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val delta = Gdx.graphics.deltaTime
        currentScreen?.render(delta)
        currentView?.render(delta)
        debugView?.render(delta)
        renderer.end()

        if (!resizing) {
            val f = if (client.isFocused() || backgroundFPS == 0) targetFPS else backgroundFPS
            if (f > 0 && (f < 60 || !vSync)) {
                Sync.sync(f)
            }
        } else {
            resizing = false
        }
    }

    override fun dispose() {
        currentScreen?.dispose()
        currentView?.dispose()
        debugView?.dispose()
        renderer.dispose()
        VisUI.dispose()
    }

    private fun resetCamera() {
        camera.zoom = 1f
        camera.position.setZero()
        camera.direction.set(0f, 0f, -1f)
        camera.up.set(0f, 1f, 0f)
        camera.update()
    }

    private fun setScreen(screen: AbstractScreen) {
        resetCamera()
        currentScreen?.apply {
            inputProcessors.remove(this)
            dispose()
        }

        currentScreen = screen
        val s = currentScreen ?: return
        inputProcessors.add(s)
        s.show()
    }

    private fun setView(view: AbstractView?) {
        currentView?.apply {
            inputProcessors.remove(stage)
            dispose()
        }

        currentView = view
        view?.apply {
            inputProcessors.add(0, stage)
        }
    }

    override fun pause() {
        mouseMoved = false
        currentScreen?.pause()
        currentView?.pause()
        debugView?.pause()
    }

    override fun resume() {
        currentScreen?.resume()
        currentView?.resume()
        debugView?.resume()
    }

    override fun resize(width: Int, height: Int) {
        resizing = true
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
        uiViewport.update(width, height, true)
        currentScreen?.resize(width, height)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (processor in inputProcessors) {
            if (processor.touchUp(screenX, screenY, pointer, button))
                return true
        }

        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        mouseMoved = true
        for (processor in inputProcessors) {
            if (processor.mouseMoved(screenX, screenY))
                return true
        }

        return false
    }

    override fun keyTyped(character: Char): Boolean {
        for (processor in inputProcessors) {
            if (processor.keyTyped(character))
                return true
        }

        return false
    }

    override fun scrolled(amount: Int): Boolean {
        for (processor in inputProcessors) {
            if (processor.scrolled(amount))
                return true
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        for (processor in inputProcessors) {
            if (processor.keyUp(keycode))
                return true
        }

        if (debug) {
            if (keycode == Input.Keys.F11) {
                if (currentScreen is TestScreen) {
                    setScreen(FieldScreen())
                    setView(null)
                } else {
                    setScreen(TestScreen())
                    setView(AbstractView.createView<TestView>())
                }

                return true
            }
            if (keycode == Input.Keys.F9) {
                vSync = !vSync
                targetFPS = if (vSync) 60 else 0
                Gdx.graphics.setVSync(vSync)
            }
            if (keycode == Input.Keys.F10) {
                Runtime.getRuntime().gc()
            }
            if (keycode == Input.Keys.F12) {
                DebugView.show = !DebugView.show
            }
        }

        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        for (processor in inputProcessors) {
            if (processor.touchDragged(screenX, screenY, pointer))
                return true
        }

        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.ESCAPE) {
            unfocusAll()
        }

        for (processor in inputProcessors) {
            if (processor.keyDown(keycode))
                return true
        }

        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        unfocusAll()
        for (processor in inputProcessors) {
            if (processor.touchDown(screenX, screenY, pointer, button))
                return true
        }

        return false
    }

    private fun unfocusAll() {
        currentView?.stage?.unfocusAll()
    }

    fun focusChanged() {
        if (pauseOnBackground) {
            if (client.isFocused()) {
                resume()
            } else {
                pause()
            }
        }
    }

    companion object {
        var debug = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
            private set
        val lmlParser: LmlParser by lazy { VisLml.parser().i18nBundle(Marsh.I18N["assets/locale/UI"]).build() }

        lateinit var instance: Jungle
            private set
    }
}
