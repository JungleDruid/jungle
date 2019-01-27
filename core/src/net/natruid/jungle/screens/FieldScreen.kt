package net.natruid.jungle.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Input
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.RenderSystem
import net.natruid.jungle.systems.TileSystem

class FieldScreen : AbstractScreen(PooledEngine(400, 3600, 400, 3600)) {
    init {
        engine.addSystem(CameraMovementSystem())
        engine.addSystem(RenderSystem())
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
        super.keyUp(keycode)
        if (keycode == Input.Keys.R) {
            engine.removeAllEntities()
            val tiles = engine.getSystem(TileSystem::class.java)
            tiles.create(20, 20)
            return true
        }

        return false
    }
}