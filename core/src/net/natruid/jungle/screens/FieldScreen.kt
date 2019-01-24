package net.natruid.jungle.screens

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.ashley.add
import ktx.ashley.entity
import ktx.math.vec3
import net.natruid.jungle.components.RectComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.systems.CameraMovementSystem
import net.natruid.jungle.systems.GridRenderSystem
import net.natruid.jungle.systems.RenderSystem
import java.lang.Math.random

class FieldScreen : AbstractScreen() {
    init {
        camera.translate(400f, 300f)
        camera.update()
        engine.addSystem(CameraMovementSystem(camera))
        engine.addSystem(RenderSystem(camera))
        engine.addSystem(GridRenderSystem(camera))
        generateField()
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.APOSTROPHE) {
            val grid = engine.getSystem(GridRenderSystem::class.java)
            grid.show = !grid.show
        }
        if (keycode == Input.Keys.R) {
            engine.removeAllEntities()
            generateField()
            return true
        }

        return false
    }

    private fun generateField() {
        for (x in 0..19) {
            for (y in 0..19) {
                val isBlock = random() > 0.8 && x > 0 && y > 0
                engine.add {
                    entity {
                        with<TransformComponent> {
                            position = vec3(x * 64f, y * 64f)
                        }
                        with<RectComponent> {
                            width = 64f
                            height = 64f
                            color = if (!isBlock) Color(
                                    random().toFloat() * 0.1f + 0.2f,
                                    random().toFloat() * 0.1f + 0.4f,
                                    0f,
                                    1f
                            ) else Color(
                                    random().toFloat() * 0.1f,
                                    0f,
                                    random().toFloat() * 0.7f + 0.1f,
                                    1f
                            )
                            type = ShapeRenderer.ShapeType.Filled
                        }
                    }
                }
            }
        }
    }
}