package net.natruid.jungle.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.ashley.add
import ktx.ashley.entity
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.GridRenderSystem
import net.natruid.jungle.systems.RenderSystem
import net.natruid.jungle.utils.Scout

class TestScreen : AbstractScreen() {
    init {
        engine.addSystem(RenderSystem())
        engine.addSystem(CameraMovementSystem())
        engine.addSystem(GridRenderSystem())
        engine.add {
            entity {
                with<TransformComponent>()
                with<TextureComponent> {
                    region = TextureRegion(Texture(Scout["assets/img/test/badlogic.jpg"]))
                }
            }
            entity {
                with<TransformComponent>()
                with<LabelComponent> {
                    text = "測試 test with a long text abcdefghijklmnopqrstuvwxyz"
                    color.set(Color.RED)
                    width = 300f
                }
            }
        }
    }
}