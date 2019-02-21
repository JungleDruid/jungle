package net.natruid.jungle.screens

import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.CameraControlSystem
import net.natruid.jungle.systems.RenderSystem
import net.natruid.jungle.utils.Scout

class TestScreen : AbstractScreen(WorldConfigurationBuilder().with(
    CameraControlSystem(),
    RenderSystem()
).build()) {
    init {
        val e = world.create()
        world.getMapper(TransformComponent::class.java).create(e)
        world.getMapper(TextureComponent::class.java).create(e).apply {
            region = TextureRegion(Texture(Scout["assets/img/test/badlogic.jpg"]))
        }
        world.getMapper(LabelComponent::class.java).create(e).apply {
            text = "測試 test with a long text abcdefghijklmnopqrstuvwxyz"
            color = Color.RED
            width = 300f
        }
    }
}
