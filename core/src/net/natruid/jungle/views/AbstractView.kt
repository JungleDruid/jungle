package net.natruid.jungle.views

import com.badlogic.gdx.scenes.scene2d.Stage
import net.natruid.jungle.core.Jungle

abstract class AbstractView : Stage(Jungle.instance.uiViewport) {
    open fun render(delta: Float) {
        act(delta)
        draw()
    }

    open fun show() {}

    open fun hide() {}

    open fun pause() {}

    open fun resume() {}
}
