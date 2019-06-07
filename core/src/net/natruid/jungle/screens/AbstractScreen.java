package net.natruid.jungle.screens;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.WorldConfigurationBuilder.Priority;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import net.natruid.jungle.systems.CameraSystem;
import net.natruid.jungle.systems.render.RenderBatchSystem;

public abstract class AbstractScreen implements Screen, InputProcessor, Disposable {
    protected World world;
    private RenderBatchSystem renderBatchSystem = new RenderBatchSystem();

    public AbstractScreen() {
        createWorld();
    }

    abstract WorldConfigurationBuilder getConfiguration(WorldConfigurationBuilder builder);

    private void createWorld() {
        world = new World(getConfiguration(new WorldConfigurationBuilder())
            .with(Priority.LOW, new CameraSystem())
            .with(Priority.LOW, renderBatchSystem)
            .alwaysDelayComponentRemoval(true)
            .build());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        world.setDelta(delta);
        world.process();
    }

    @Override
    public void resize(int width, int height) {
        world.getSystem(CameraSystem.class).resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        world.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).keyDown(keycode)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).keyUp(keycode)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).keyTyped(character)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).touchDown(screenX, screenY, pointer, button)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).touchUp(screenX, screenY, pointer, button)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).touchDragged(screenX, screenY, pointer)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).mouseMoved(screenX, screenY)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        for (BaseSystem system : world.getSystems()) {
            if (system.isEnabled() && system instanceof InputProcessor) {
                if (((InputProcessor) system).scrolled(amount)) return true;
            }
        }
        return false;
    }
}
