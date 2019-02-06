package net.natruid.jungle.screens

import com.artemis.Aspect
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.core.Jungle
import net.natruid.jungle.systems.*
import net.natruid.jungle.utils.extensions.forEach

class FieldScreen : AbstractScreen(WorldConfigurationBuilder().with(
    TagManager(),
    CameraMovementSystem(),
    TileSystem(),
    UnitManagementSystem(),
    PathFollowingSystem(),
    RenderSystem()
).build()) {
    init {
        init()
    }

    private fun init() {
        world.getSystem(TileSystem::class.java).create(20, 20)
        world.getSystem(UnitManagementSystem::class.java)
            .addUnit(faction = UnitComponent.Faction.PLAYER, speed = 6f)
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
            world.getSystem(UnitManagementSystem::class.java).clean()
            world.aspectSubscriptionManager.get(Aspect.all()).entities.forEach {
                world.delete(it)
            }
            init()
            return true
        }

        return false
    }
}
