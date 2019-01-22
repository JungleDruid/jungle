package net.natruid.jungle.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.RenderSystem

abstract class AbstractScreen : Screen, InputProcessor {
    protected val engine = PooledEngine()
    protected val renderSystem = RenderSystem(Jungle.instance!!.batch)
    val stage = Stage(ScreenViewport())

    init {
        engine.addSystem(renderSystem)
    }

    override fun render(delta: Float) {
        engine.update(delta)
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        engine.removeAllEntities()
        engine.clearPools()
        stage.dispose()
    }

    override fun resize(width: Int, height: Int) {
        renderSystem.updateCamera(width, height)
        stage.viewport.update(width, height, true)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage.touchUp(screenX, screenY, pointer, button)
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return stage.mouseMoved(screenX, screenY)
    }

    override fun keyTyped(character: Char): Boolean {
        return stage.keyTyped(character)
    }

    override fun scrolled(amount: Int): Boolean {
        return stage.scrolled(amount)
    }

    override fun keyUp(keycode: Int): Boolean {
        return stage.keyUp(keycode)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return stage.touchDragged(screenX, screenY, pointer)
    }

    override fun keyDown(keycode: Int): Boolean {
        return stage.keyDown(keycode)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage.touchDown(screenX, screenY, pointer, button)
    }

    override fun hide() {}

    override fun show() {}

    override fun pause() {}

    override fun resume() {}
}