package net.natruid.jungle.utils.extensions

import com.badlogic.ashley.core.Engine

fun Engine.removeAllSystems() {
    val systems = systems
    for (i in (systems.size() - 1) downTo 0) {
        removeSystem(systems[i])
    }
}
