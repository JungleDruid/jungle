package net.natruid.jungle.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.GridRenderSystem
import net.natruid.jungle.systems.RenderSystem
import net.natruid.jungle.utils.Tiles

class FieldScreen : AbstractScreen(PooledEngine(400, 3600, 400, 3600)) {
    private val tiles = Tiles.obtain(engine)

    init {
        camera.translate(400f, 300f)
        camera.update()
        engine.addSystem(CameraMovementSystem(camera))
        engine.addSystem(RenderSystem(camera))
        engine.addSystem(GridRenderSystem(camera))
        tiles.create(20, 20)
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.APOSTROPHE) {
            val grid = engine.getSystem(GridRenderSystem::class.java)
            grid.show = !grid.show
        }
        if (keycode == Input.Keys.R) {
            engine.removeAllEntities()
            tiles.create(20, 20)
            tiles[0, 0]?.getComponent(RectComponent::class.java)?.color?.set(Color.YELLOW)
            return true
        }

        return false
    }

    override fun dispose() {
        tiles.free()
        super.dispose()
    }
}