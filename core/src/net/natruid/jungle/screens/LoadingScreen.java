package net.natruid.jungle.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LoadingScreen extends ScreenAdapter {
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private boolean done = false;
    private int maxProgress = 1;
    private int progress = 0;
    private float fakeProgress = 0;

    public void init(int maxProgress) {
        this.maxProgress = maxProgress;
        progress = 0;
        done = false;
    }

    public boolean isDone() {
        return done;
    }

    public void progress() {
        progress += 1;
        fakeProgress = 0;
    }

    public void finish() {
        progress = maxProgress;
        fakeProgress = 0;
    }

    @Override
    public void render(float delta) {
        if (done) return;
        if (progress < maxProgress) {
            fakeProgress = Math.min(fakeProgress + delta, 0.9f);
        }
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(100f, 100f, Gdx.graphics.getWidth() - 200f, 20f);
        shapeRenderer.end();
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(100f, 100f, Gdx.graphics.getWidth() - 200f, 20f);
        shapeRenderer.end();
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(100f, 100f, (Gdx.graphics.getWidth() - 200f) * (progress + fakeProgress) / maxProgress, 20f);
        shapeRenderer.end();
        if (progress >= maxProgress) {
            done = true;
        }
    }
}
