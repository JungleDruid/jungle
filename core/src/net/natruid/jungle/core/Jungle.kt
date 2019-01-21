package net.natruid.jungle.core

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.kotcrab.vis.ui.VisUI
import net.natruid.jungle.screens.AbstractScreen
import net.natruid.jungle.screens.TestScreen
import net.natruid.jungle.utils.MySkin

class Jungle : Game() {
    override fun create() {
        Data.load()
        VisUI.load(MySkin("ui/jungle.json"))

        setScreen(TestScreen())
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render()
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            setScreen(TestScreen())
        }
    }

    private fun setScreen(screen: AbstractScreen) {
        if (this.screen != null) {
            this.screen.dispose()
        }

        this.screen = screen
        Gdx.input.inputProcessor = screen
    }
}
