package net.natruid.jungle.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen

abstract class AbstractScreen(protected val engine: PooledEngine = PooledEngine()) : Screen, InputProcessor {
    override fun render(delta: Float) {
        engine.update(delta)
    }

    override fun dispose() {
        engine.removeAllEntities()
        engine.clearPools()
    }

    override fun resize(width: Int, height: Int) {}

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun hide() {}

    override fun show() {}

    override fun pause() {}

    override fun resume() {}
}