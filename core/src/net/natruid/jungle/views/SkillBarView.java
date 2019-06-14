package net.natruid.jungle.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.VisTable;
import net.natruid.jungle.core.Sky;

public class SkillBarView extends AbstractView {
    private final VisTable apBar = new VisTable();
    private final Array<ApPointCircle> apCircleList = new Array<>();

    public SkillBarView() {
        VisTable table = new VisTable();
        table.setFillParent(true);
        table.align(Align.bottom);
        table.add(apBar);
        addActor(table);
    }

    @Override
    public void show() {
        for (int i = 0; i < 6; i++) {
            apCircleList.add(Pools.obtain(ApPointCircle.class));
        }
        apCircleList.forEach(apBar::add);
        apBar.setVisible(false);
    }

    @Override
    public void hide() {
        apCircleList.forEach(apPointCircle -> {
            apBar.removeActor(apPointCircle);
            Pools.free(apPointCircle);
        });
        apCircleList.clear();
    }

    public void setAp(int value, int preview) {
        for (int i = 0; i < apCircleList.size; i++) {
            Color color;
            if (preview >= 0 && i >= value - preview && i < value) {
                color = Color.RED;
            } else if (preview >= 0 && i < value - preview || preview < 0 && i < value) {
                color = Color.GREEN;
            } else {
                color = Color.GRAY;
            }
            apCircleList.get(i).setColor(color);
        }
        apBar.setVisible(true);
    }

    public void setAp(int value) {
        setAp(value, -1);
    }

    public void hideAp() {
        apBar.setVisible(false);
    }

    static class ApPointCircle extends Widget implements Pool.Poolable {
        private static ShapeRenderer shapeRenderer;

        ApPointCircle() {
            setSize(30, 30);
        }

        private static ShapeRenderer getShapeRenderer() {
            if (shapeRenderer == null) {
                shapeRenderer = new ShapeRenderer();
            }
            return shapeRenderer;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            ShapeRenderer shapeRenderer = getShapeRenderer();
            batch.end();
            shapeRenderer.setProjectionMatrix(Sky.jungle.getUiViewport().getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(getColor());
            shapeRenderer.circle(getX() + getWidth() / 2, getY() + getHeight() / 2, 10f);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(getX() + getWidth() / 2, getY() + getHeight() / 2, 10f);
            shapeRenderer.end();
            batch.begin();
        }

        @Override
        public float getPrefWidth() {
            return getWidth();
        }

        @Override
        public float getPrefHeight() {
            return getHeight();
        }

        @Override
        public void reset() {
            setColor(1f, 1f, 1f, 1f);
        }
    }
}
