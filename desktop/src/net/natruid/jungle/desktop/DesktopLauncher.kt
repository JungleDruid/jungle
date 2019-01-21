package net.natruid.jungle.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import net.natruid.jungle.core.Jungle

fun main(arg: Array<String>) {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Jungle")
    config.setWindowedMode(1024, 768)
    Lwjgl3Application(Jungle(), config)
}
