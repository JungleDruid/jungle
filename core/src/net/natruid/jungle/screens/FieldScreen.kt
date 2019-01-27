package net.natruid.jungle.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.GridRenderSystem
import net.natruid.jungle.systems.RenderSystem
import net.natruid.jungle.systems.TileSystem

class FieldScreen : AbstractScreen(PooledEngine(400, 3600, 400, 3600)) {
    init {
        engine.addSystem(CameraMovementSystem())
        engine.addSystem(RenderSystem())
        engine.addSystem(GridRenderSystem())
        val tiles = TileSystem()
        engine.addSystem(tiles)
        tiles.create(20, 20)
    }

    override fun show() {
        super.show()
        val camera = Jungle.instance.camera
        camera.translate(400f, 300f)
        camera.update()
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.APOSTROPHE) {
            val grid = engine.getSystem(GridRenderSystem::class.java)
            grid.show = !grid.show
        }
        if (keycode == Input.Keys.R) {
            engine.removeAllEntities()
            val tiles = engine.getSystem(TileSystem::class.java)
            tiles.create(20, 20)
            tiles[2, 4]?.getComponent(RectComponent::class.java)?.color?.set(Color.YELLOW)
            return true
        }

        return false
    }
}