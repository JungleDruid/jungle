package net.natruid.jungle.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import ktx.assets.disposeSafely
import net.natruid.jungle.screens.AbstractScreen
import net.natruid.jungle.screens.FieldScreen
import net.natruid.jungle.screens.LoadingScreen
import net.natruid.jungle.screens.TestScreen
import net.natruid.jungle.utils.Bark
import net.natruid.jungle.utils.Client
import net.natruid.jungle.utils.Logger
import net.natruid.jungle.utils.Sync
import net.natruid.jungle.views.AbstractView
import net.natruid.jungle.views.DebugView
import net.natruid.jungle.views.TestView
import java.lang.management.ManagementFactory

class JungleA(private val client: Client, debug: Boolean = false) : ApplicationListener, InputProcessor {
    val isMouseMoved: Boolean
        get() = mouseMoved
    val isDebug: Boolean
        get() = debug
    val uiViewport by lazy { ScreenViewport() }
    val loadingScreen by lazy { LoadingScreen() }

    var mouseMoved = false
        private set
    var time = 0f
        private set
    var debugView: DebugView? = null
        private set

    private var currentScreen: AbstractScreen? = null
    private val viewList = ArrayList<AbstractView>()
    private var targetFPS = 60
    private var backgroundFPS = 10
    private var pauseOnBackground = true
    private var resizing = false
    private var vSync = true
    private var paused = false
    private val sync = Sync()

    init {
//        Sky.jungle = this
        Companion.debug = Companion.debug || debug
    }

    override fun create() {
        Sky.scout = Scout()
        loadingScreen.init(5)

        Logger.startWatch("Initialization")
        Logger.debug("Initializing...")

        client.init()
        loadingScreen.progress()

        try {
            Marsh.load()
        } catch (e: Exception) {
            Logger.error("Data loading failed", e)
        }
        loadingScreen.progress()

        try {
            VisUI.load(Bark("assets/ui/jungle.json"))
        } catch (e: Exception) {
            Logger.error("Skin loading failed.")
        }
        loadingScreen.progress()

        try {
            val bundle = Marsh.I18N["assets/locale/UI"]
            client.setTitle(bundle["title"])
        } catch (e: Exception) {
            Logger.error("I18n bundle loading failed.", e)
        }
        loadingScreen.progress()

        if (debug) {
            debugView = DebugView()
            DebugView.show = true
        }
        setScreen(FieldScreen())
        Gdx.graphics.setVSync(vSync)
        Logger.info("Game initialized.")
        loadingScreen.finish()

        Gdx.input.inputProcessor = this@JungleA

        Logger.stopWatch("Initialization")
    }

    override fun render() {
        if (paused && loadingScreen.isDone) {
            sync.sync(1)
            return
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val delta = Gdx.graphics.deltaTime

        if (!loadingScreen.isDone) {
            loadingScreen.render(delta)
            sync.sync(10)
            return
        }

        time += delta

        currentScreen?.render(delta)
        viewList.forEach {
            it.render(delta)
        }
        debugView?.render(delta)

        if (!resizing) {
            val f = if (client.isFocused || backgroundFPS == 0) targetFPS else backgroundFPS
            if (f > 0 && (f < 60 || !vSync)) {
                sync.sync(f)
            }
        } else {
            resizing = false
        }
    }

    override fun dispose() {
        loadingScreen.dispose()
        currentScreen?.dispose()
        viewList.forEach {
            it.disposeSafely()
        }
        viewList.clear()
        debugView?.dispose()
        VisUI.dispose()
    }

    private fun setScreen(screen: AbstractScreen) {
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
        paused = true
        mouseMoved = false
        currentScreen?.pause()
        viewList.forEach {
            it.pause()
        }
        debugView?.pause()
    }

    override fun resume() {
        paused = false
        currentScreen?.resume()
        viewList.forEach {
            it.pause()
        }
        debugView?.resume()
    }

    override fun resize(width: Int, height: Int) {
        resizing = true
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
                    showView(TestView())
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
            it.unfocusAll()
        }
    }

    fun focusChanged() {
        if (pauseOnBackground) {
            if (client.isFocused) {
                resume()
            } else {
                pause()
            }
        }
    }

    companion object {
        var debug = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
            private set
    }
}
