package net.natruid.jungle.views;

import com.badlogic.gdx.scenes.scene2d.Stage;
import net.natruid.jungle.core.Sky;

public abstract class AbstractView extends Stage {
    public AbstractView() {
        super(Sky.jungle.getUiViewport());
    }

    public void render(float delta) {
        this.act(delta);
        this.draw();
    }

    public void show() {
    }

    public void hide() {
    }

    public void pause() {
    }

    public void resume() {
    }
}

