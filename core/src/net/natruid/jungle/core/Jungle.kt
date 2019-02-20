package net.natruid.jungle.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.vis.util.VisLml
import com.kotcrab.vis.ui.VisUI
import ktx.assets.disposeSafely
import net.natruid.jungle.screens.AbstractScreen
import net.natruid.jungle.screens.FieldScreen
import net.natruid.jungle.screens.TestScreen
import net.natruid.jungle.utils.*
import net.natruid.jungle.views.AbstractView
import net.natruid.jungle.views.DebugView
import net.natruid.jungle.views.TestView
import java.lang.management.ManagementFactory
import java.util.*

class Jungle(private val client: Client, debug: Boolean = false) : ApplicationListener, InputProcessor {
    val renderer by lazy { RendererHelper() }
    val camera by lazy { OrthographicCamera() }
    val uiViewport by lazy { ScreenViewport() }

    var mouseMoved = false
        private set
    var time = 0f
        private set

    private var currentScreen: AbstractScreen? = null
    private val viewList = ArrayList<AbstractView>()
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
        time += delta
        currentScreen?.render(delta)
        viewList.forEach {
            it.render(delta)
        }
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
        viewList.forEach {
            it.dispose()
        }
        viewList.clear()
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
        currentScreen?.disposeSafely()

        currentScreen = screen
        val s = currentScreen ?: return
        s.show()
    }

    fun showView(view: AbstractView) {
        if (!viewList.contains(view)) {
            viewList.add(view)
            view.show()
        }
    }

    fun hideView(view: AbstractView) {
        if (viewList.remove(view)) {
            view.hide()
        }
    }

    fun hideLastView(): AbstractView? {
        if (viewList.size == 0) return null
        return viewList.removeAt(viewList.size - 1).apply { hide() }
    }

    fun destroyView(view: AbstractView) {
        hideView(view)
        view.disposeSafely()
    }

    fun destroyAllViews() {
        viewList.forEach {
            it.disposeSafely()
        }
        viewList.clear()
    }

    override fun pause() {
        mouseMoved = false
        currentScreen?.pause()
        viewList.forEach {
            it.pause()
        }
        debugView?.pause()
    }

    override fun resume() {
        currentScreen?.resume()
        viewList.forEach {
            it.pause()
        }
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
        for (view in viewList) {
            if (view.touchUp(screenX, screenY, pointer, button))
                return true
        }

        return currentScreen?.touchUp(screenX, screenY, pointer, button) ?: false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        mouseMoved = true
        for (view in viewList) {
            if (view.mouseMoved(screenX, screenY))
                return true
        }

        return currentScreen?.mouseMoved(screenX, screenY) ?: false
    }

    override fun keyTyped(character: Char): Boolean {
        for (view in viewList) {
            if (view.keyTyped(character))
                return true
        }

        return currentScreen?.keyTyped(character) ?: false
    }

    override fun scrolled(amount: Int): Boolean {
        for (view in viewList) {
            if (view.scrolled(amount))
                return true
        }

        return currentScreen?.scrolled(amount) ?: false
    }

    override fun keyUp(keycode: Int): Boolean {
        for (view in viewList) {
            if (view.keyUp(keycode))
                return true
        }

        if (currentScreen?.keyUp(keycode) == true) {
            return true
        }

        if (debug) {
            if (keycode == Input.Keys.F11) {
                if (currentScreen is TestScreen) {
                    destroyAllViews()
                    setScreen(FieldScreen())
                } else {
                    setScreen(TestScreen())
                    showView(AbstractView.createView<TestView>())
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
        for (view in viewList) {
            if (view.touchDragged(screenX, screenY, pointer))
                return true
        }

        return currentScreen?.touchDragged(screenX, screenY, pointer) ?: false
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.ESCAPE) {
            unfocusAll()
        }

        for (view in viewList) {
            if (view.keyDown(keycode))
                return true
        }

        return currentScreen?.keyDown(keycode) ?: false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        unfocusAll()
        for (view in viewList) {
            if (view.touchDown(screenX, screenY, pointer, button))
                return true
        }

        return currentScreen?.touchDown(screenX, screenY, pointer, button) ?: false
    }

    private fun unfocusAll() {
        viewList.forEach {
            it.stage?.unfocusAll()
        }
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
