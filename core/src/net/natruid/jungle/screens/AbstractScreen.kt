package net.natruid.jungle.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.WorldConfigurationBuilder.Priority
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.Disposable
import net.natruid.jungle.systems.CameraSystem
import net.natruid.jungle.systems.render.RenderBatchSystem

abstract class AbstractScreen : Screen, InputProcessor, Disposable {
    protected val renderBatchSystem = RenderBatchSystem()
    protected lateinit var world: World

    abstract fun getConfiguration(builder: WorldConfigurationBuilder): WorldConfigurationBuilder

    init {
        createWorld()
    }

    private fun createWorld() {
        world = World(getConfiguration(WorldConfigurationBuilder())
            .with(Priority.LOW, CameraSystem())
            .with(Priority.LOW, renderBatchSystem)
            .build())
    }

    override fun render(delta: Float) {
        world.setDelta(delta)
        world.process()
    }

    override fun dispose() {
        world.dispose()
    }

    override fun resize(width: Int, height: Int) {
        world.getSystem(CameraSystem::class.java).resize(width, height)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.touchUp(screenX, screenY, pointer, button)) return true
            }
        }
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.mouseMoved(screenX, screenY)) return true
            }
        }
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.keyTyped(character)) return true
            }
        }
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.scrolled(amount)) return true
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.keyUp(keycode)) return true
            }
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.touchDragged(screenX, screenY, pointer)) return true
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.keyDown(keycode)) return true
            }
        }
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        for (system in world.systems) {
            if (system.isEnabled && system is InputProcessor) {
                if (system.touchDown(screenX, screenY, pointer, button)) return true
            }
        }
        return false
    }

    override fun hide() {}

    override fun show() {}

    override fun pause() {}

    override fun resume() {}
}
