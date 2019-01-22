package net.natruid.jungle.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.utils.DesktopClient
import org.lwjgl.glfw.GLFW

object DesktopLauncher : DesktopClient {
    private var window = 0L

    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("Jungle")
        config.setWindowedMode(1024, 768)
        config.setResizable(false)
        Lwjgl3Application(Jungle(this), config)
    }

    override fun resize(width: Int, height: Int): Boolean {
        if (checkWindow()) {
            GLFW.glfwSetWindowSize(window, width, height)
            return true
        }

        return false
    }

    override fun setTitle(title: String): Boolean {
        if (checkWindow()) {
            GLFW.glfwSetWindowTitle(window, title)
            return true
        }

        return false
    }

    private fun checkWindow(): Boolean {
        if (window <= 0L) {
            window = GLFW.glfwGetCurrentContext()
        }

        return window > 0L
    }
}
