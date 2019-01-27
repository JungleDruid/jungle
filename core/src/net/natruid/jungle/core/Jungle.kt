package net.natruid.jungle.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.vis.util.VisLml
import com.kotcrab.vis.ui.VisUI
import net.natruid.jungle.screens.AbstractScreen
import net.natruid.jungle.screens.FieldScreen
import net.natruid.jungle.screens.TestScreen
import net.natruid.jungle.utils.Bark
import net.natruid.jungle.utils.Client
import net.natruid.jungle.utils.RendererHelper
import net.natruid.jungle.utils.Sync
import net.natruid.jungle.views.AbstractView
import net.natruid.jungle.views.DebugView
import net.natruid.jungle.views.TestView
import java.lang.management.ManagementFactory

class Jungle(private val client: Client) : ApplicationListener, InputProcessor {
    val renderer by lazy { RendererHelper() }
    val camera by lazy { OrthographicCamera() }
    val uiViewport by lazy { ScreenViewport() }

    private var currentScreen: AbstractScreen? = null
    private var currentView: AbstractLmlView? = null
    private val inputProcessors = Array<InputProcessor>()
    private var debugView: DebugView? = null
    private var targetFPS = 30
    private var backgroundFPS = 10
    private var pauseOnBackground = true
    private var resizing = false

    init {
        instance = this
    }

    override fun create() {
        client.init()

        Marsh.load()

        Gdx.input.inputProcessor = this

        VisUI.load(Bark("assets/ui/jungle.json"))

        val bundle = Marsh.I18N["assets/locale/UI"]
        client.setTitle(bundle["title"])

        setScreen(FieldScreen())
        debugView = AbstractView.createView()
        Gdx.graphics.setVSync(false)
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
            if (targetFPS > 0) {
                Sync.sync(if (client.isFocused() || backgroundFPS <= 0) {
                    targetFPS
                } else {
                    backgroundFPS
                })
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

    fun resetCamera() {
        camera.zoom = 1f
        camera.position.setZero()
        camera.direction.set(0f, 0f, -1f)
        camera.up.set(0f, 1f, 0f)
        camera.update()
    }

    private fun setScreen(screen: AbstractScreen) {
        resetCamera()
        if (currentScreen != null) inputProcessors.removeValue(currentScreen, true)
        currentScreen?.dispose()

        currentScreen = screen
        val s = currentScreen ?: return
        inputProcessors.add(s)
        s.show()
    }

    private fun setView(view: AbstractView?) {
        val v = currentView
        if (v != null) {
            inputProcessors.removeValue(v.stage, true)
            v.dispose()
        }

        currentView = view
        if (view != null) {
            inputProcessors.insert(0, view.stage)
        }
    }

    override fun pause() {
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
                    debugView = AbstractView.createView()
                }

                return true
            }
            if (keycode == Input.Keys.F12) {
                DebugView.show = !DebugView.show
                targetFPS = if (DebugView.show) 0 else 30
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

    fun addInputProcessor(processor: InputProcessor) {
        inputProcessors.add(processor)
    }

    fun removeInputProcessor(processor: InputProcessor) {
        inputProcessors.removeValue(processor, true)
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
        val debug = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
        val lmlParser: LmlParser by lazy { VisLml.parser().i18nBundle(Marsh.I18N["assets/locale/UI"]).build() }

        private var hInstance: Jungle? = null
        var instance: Jungle
            get() = hInstance!!
            private set(value) {
                hInstance = value
            }
    }
}
