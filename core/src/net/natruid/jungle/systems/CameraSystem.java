package net.natruid.jungle.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.Point;

public class CameraSystem extends BaseSystem implements InputProcessor {
    private static final float speed = 512f;
    private static final float maxZoom = 2f;
    private static final float minZoom = 0.25f;
    private static final float zoomStep = minZoom;

    public final OrthographicCamera camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    public final OrthographicCamera uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    private final Vector2 velocity = new Vector2();
    private final Vector2 vector2 = new Vector2();
    private final Point dragPoint = new Point();
    public Rectangle cropRect = null;
    private boolean dragStarted = false;

    private void setZoom(float value) {
        float oldZoom = camera.zoom;
        camera.zoom = Math.max(minZoom, Math.min(maxZoom, value));
        float diff = camera.zoom - oldZoom;
        if (diff != 0f) {
            float x = (Gdx.input.getX() - Gdx.graphics.getWidth() / 2f) * -diff;
            float y = (Gdx.input.getY() - Gdx.graphics.getHeight() / 2f) * diff;
            camera.translate(x, y);
            clamp();
            camera.update();
        }
    }

    private void clamp() {
        if (cropRect != null) {
            Vector3 position = camera.position;
            position.x = Math.max(cropRect.x, Math.min(cropRect.x + cropRect.width, position.x));
            position.y = Math.max(cropRect.y, Math.min(cropRect.y + cropRect.height, position.y));
        }
    }

    @Override
    protected void processSystem() {
        if (!velocity.isZero()) {
            vector2.set(velocity);
            camera.translate(vector2.scl(speed * world.delta * camera.zoom));
            clamp();
            camera.update();
            if (Sky.jungle.isMouseMoved())
                Sky.jungle.mouseMoved(Gdx.input.getX(), Gdx.input.getY());
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            velocity.y += 1;
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            velocity.y -= 1;
        }
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            velocity.x -= 1;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            velocity.x += 1;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            velocity.y -= 1;
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
            velocity.y += 1;
        }
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            velocity.x += 1;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            velocity.x -= 1;
        }

        if (keycode == Input.Keys.EQUALS) {
            setZoom(camera.zoom - zoomStep);
        }
        if (keycode == Input.Keys.MINUS) {
            setZoom(camera.zoom + zoomStep);
        }

        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        setZoom(camera.zoom + amount * zoomStep);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        dragPoint.set(screenX, screenY);
        dragStarted = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragStarted = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragStarted) return false;
        camera.translate((dragPoint.x - screenX) * camera.zoom, (screenY - dragPoint.y) * camera.zoom);
        camera.update();
        dragPoint.set(screenX, screenY);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        uiCamera.viewportWidth = width;
        uiCamera.viewportHeight = height;
        uiCamera.position.set(width / 2f, height / 2f, 0f);
        uiCamera.update();
    }
}
