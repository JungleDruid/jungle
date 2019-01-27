package net.natruid.jungle.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import net.natruid.jungle.systems.RenderSystem
import net.natruid.jungle.utils.extensions.removeAllSystems

abstract class AbstractScreen(protected val engine: PooledEngine = PooledEngine()) : Screen, InputProcessor {
    override fun render(delta: Float) {
        engine.update(delta)
    }

    override fun dispose() {
        engine.removeAllEntities()
        engine.removeAllSystems()
    }

    override fun resize(width: Int, height: Int) {}

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.touchUp(screenX, screenY, pointer, button)
            }
        }
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.mouseMoved(screenX, screenY)
            }
        }
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.keyTyped(character)
            }
        }
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.scrolled(amount)
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.keyUp(keycode)
            }
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.touchDragged(screenX, screenY, pointer)
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.keyDown(keycode)
            }
        }
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (system in engine.systems) {
            if (system is InputProcessor && system.checkProcessing()) {
                system.touchDown(screenX, screenY, pointer, button)
            }
        }
        return false
    }

    override fun hide() {}

    override fun show() {}

    override fun pause() {
        for (system in engine.systems) {
            if (system !is RenderSystem) system.setProcessing(false)
        }
    }

    override fun resume() {
        for (system in engine.systems) {
            system.setProcessing(true)
        }
    }
}
