package net.natruid.jungle.screens

import com.artemis.World
import com.artemis.WorldConfiguration
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import net.natruid.jungle.systems.RenderSystem

abstract class AbstractScreen(configuration: WorldConfiguration) : Screen, InputProcessor {
    protected val world = World(configuration)

    override fun render(delta: Float) {
        world.setDelta(delta)
        world.process()
    }

    override fun dispose() {
        world.dispose()
    }

    override fun resize(width: Int, height: Int) {}

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.touchUp(screenX, screenY, pointer, button)
            }
        }
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.mouseMoved(screenX, screenY)
            }
        }
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.keyTyped(character)
            }
        }
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.scrolled(amount)
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.keyUp(keycode)
            }
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.touchDragged(screenX, screenY, pointer)
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.keyDown(keycode)
            }
        }
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                system.touchDown(screenX, screenY, pointer, button)
            }
        }
        return false
    }

    override fun hide() {}

    override fun show() {}

    override fun pause() {
        for (system in world.systems) {
            if (system !is RenderSystem) system.isEnabled = false
        }
    }

    override fun resume() {
        for (system in world.systems) {
            system.isEnabled = true
        }
    }
}
