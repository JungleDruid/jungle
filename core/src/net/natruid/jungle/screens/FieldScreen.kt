package net.natruid.jungle.screens

import com.badlogic.gdx.Input
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.*

class FieldScreen : AbstractScreen() {
    init {
        engine.addSystem(CameraMovementSystem())
        engine.addSystem(RenderSystem())
        TileSystem().let {
            engine.addSystem(it)
            it.create(20, 20)
        }
        UnitManagementSystem().let {
            engine.addSystem(it)
            it.addUnit(UnitComponent(faction = UnitComponent.Faction.PLAYER, speed = 6f))
        }
        engine.addSystem(PathFollowingSystem())
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
            engine.getSystem(TileSystem::class.java).create(20, 20)
            engine.getSystem(UnitManagementSystem::class.java).let {
                it.clean()
                it.addUnit(UnitComponent(faction = UnitComponent.Faction.PLAYER, speed = 6f))
            }
            return true
        }

        return false
    }
}