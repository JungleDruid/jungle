package net.natruid.jungle.screens

import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.render.PosComponent
import net.natruid.jungle.components.render.RenderComponent
import net.natruid.jungle.components.render.TextureComponent
import net.natruid.jungle.core.Sky
import net.natruid.jungle.systems.render.ImageRenderSystem
import net.natruid.jungle.systems.render.LabelRenderSystem

class TestScreen : AbstractScreen() {
    override fun getConfiguration(builder: WorldConfigurationBuilder): WorldConfigurationBuilder {
        return builder.with(
            ImageRenderSystem(),
            LabelRenderSystem()
        )
    }

    init {
        val e = world.create()
        world.getMapper(RenderComponent::class.java).create(e)
        world.getMapper(PosComponent::class.java).create(e)
        world.getMapper(TextureComponent::class.java).create(e).apply {
            region = TextureRegion(Texture(Sky.scout.locate("assets/img/test/badlogic.jpg")))
        }
        world.getMapper(LabelComponent::class.java).create(e).apply {
            text = "測試 test with a long text abcdefghijklmnopqrstuvwxyz"
            color.set(Color.RED)
            width = 300f
        }
    }
}
