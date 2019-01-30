package net.natruid.jungle.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.Client
import org.lwjgl.glfw.GLFW

class DesktopLauncher : Client {
    private val window by lazy { GLFW.glfwGetCurrentContext() }
    private val game = Jungle(this)
    private var focused = false

    init {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("Jungle")
        config.setWindowedMode(1024, 768)
        config.setResizable(false)
        Lwjgl3Application(game, config)
    }

    override fun init(): Boolean {
        GLFW.glfwSetWindowFocusCallback(window) { _, focused ->
            this.focused = focused
            game.focusChanged()
        }
        return true
    }

    override fun resize(width: Int, height: Int): Boolean {
        GLFW.glfwSetWindowSize(window, width, height)
        return true
    }

    override fun setTitle(title: String): Boolean {
        GLFW.glfwSetWindowTitle(window, title)
        return true
    }

    override fun isFocused(): Boolean {
        return focused
    }
}
