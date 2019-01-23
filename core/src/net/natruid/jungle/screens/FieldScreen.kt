package net.natruid.jungle.screens

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.ashley.add
import ktx.ashley.entity
import ktx.math.vec3
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.GridRenderSystem
import net.natruid.jungle.systems.RenderSystem
import kotlin.math.floor

class FieldScreen : AbstractScreen() {
    init {
        engine.addSystem(CameraMovementSystem(camera))
        engine.addSystem(RenderSystem(camera))
        engine.addSystem(GridRenderSystem(camera))
        val grassTexture = Texture("assets/img/tiles/grass.png")
        val grass = Array(9) { i ->
            TextureRegion(grassTexture, i.rem(3) * 64, i / 3 * 64, 64, 64)
        }
        for (x in 0..19) {
            for (y in 0..19) {
                engine.add {
                    entity {
                        with<TextureComponent> {
                            region = grass[floor(Math.random() * 9).toInt()]
                        }
                        with<TransformComponent> {
                            position = vec3(x * 64f, y * 64f)
                        }
                    }
                }
            }
        }
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.APOSTROPHE) {
            val grid = engine.getSystem(GridRenderSystem::class.java)
            grid.show = !grid.show
        }

        return false
    }
}