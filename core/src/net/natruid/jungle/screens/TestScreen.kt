package net.natruid.jungle.screens

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.ashley.add
import ktx.ashley.entity
import ktx.math.vec2
import ktx.vis.table
import net.natruid.jungle.components.LabelComponent
import net.natruid.jungle.components.TextureComponent
import net.natruid.jungle.components.TransformComponent
import net.natruid.jungle.core.Data

class TestScreen : AbstractScreen() {
    private val cameraSpeed = 10f

    private var cameraMovement = vec2(0f, 0f)

    init {
        engine.add {
            entity {
                with<TransformComponent>()
                with<TextureComponent> {
                    region = TextureRegion(Texture("assets/badlogic.jpg"))
                }
            }
            entity {
                with<TransformComponent>()
                with<LabelComponent> {
                    text = "測試 test with a long text abcdefghijklmnopqrstuvwxyz"
                    color = Color.RED
                    width = 300f
                }
            }
        }
        val root = table {
            setFillParent(true)
            padBottom(20f)
            padLeft(100f)
            bottom()
            left()

            label("Big 測試") { l ->
                l.padRight(20f)
                style = Label.LabelStyle(Data.Fonts["big"], Color.YELLOW)
            }
            label("Normal 測試") { l ->
                l.padRight(20f)
            }
            label("Small 測試", "small") { l ->
                l.padRight(20f)
            }
            label("newline\ntest", "small") { l ->
                l.padRight(20f)
            }
            textButton("1") { b ->
                b.width(50f).pad(5f)
                isDisabled = true
            }
            textButton("2") { b -> b.width(50f).pad(5f) }
            textButton("3") { b -> b.width(50f).pad(5f) }
            textButton("4") { b -> b.width(50f).pad(5f) }
            textButton("5") { b -> b.width(50f).pad(5f) }
            row()
            textField("TestField")
        }
        stage.addActor(root)
    }

    override fun render(delta: Float) {
        if (!cameraMovement.isZero) {
            renderSystem.camera.translate(cameraMovement)
            renderSystem.camera.update()
        }

        super.render(delta)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (super.keyDown(keycode)) {
            return true
        }

        if (keycode == Input.Keys.W) {
            cameraMovement.y += cameraSpeed
        }
        if (keycode == Input.Keys.S) {
            cameraMovement.y -= cameraSpeed
        }
        if (keycode == Input.Keys.A) {
            cameraMovement.x -= cameraSpeed
        }
        if (keycode == Input.Keys.D) {
            cameraMovement.x += cameraSpeed
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (super.keyUp(keycode)) {
            return true
        }

        if (keycode == Input.Keys.W) {
            cameraMovement.y -= cameraSpeed
        }
        if (keycode == Input.Keys.S) {
            cameraMovement.y += cameraSpeed
        }
        if (keycode == Input.Keys.A) {
            cameraMovement.x += cameraSpeed
        }
        if (keycode == Input.Keys.D) {
            cameraMovement.x -= cameraSpeed
        }

        if (keycode == Input.Keys.EQUALS) {
            renderSystem.zoom -= 0.2f
        }
        if (keycode == Input.Keys.MINUS) {
            renderSystem.zoom += 0.2f
        }

        return false
    }

    override fun scrolled(amount: Int): Boolean {
        if (stage.scrolled(amount)) {
            return true
        }

        renderSystem.zoom += amount * 0.2f
        return false
    }
}