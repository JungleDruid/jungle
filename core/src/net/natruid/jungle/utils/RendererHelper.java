package net.natruid.jungle.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import net.natruid.jungle.utils.types.RendererType;

public class RendererHelper implements Disposable {
    public final SpriteBatch batch = new SpriteBatch(1000, Shader.getDefaultShaderProgram());
    public final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector3 projection = new Vector3();
    public int begins = 0;
    public int diffs = 0;
    private RendererType rendererType = RendererType.NONE;
    private OrthographicCamera camera = null;
    private Rectangle crop = null;

    public void begin(OrthographicCamera camera, RendererType rendererType, Rectangle crop) {
        assert rendererType != RendererType.NONE;
        begins += 1;
        boolean same = this.rendererType == rendererType;

        if (!same) {
            end(false);
        }

        boolean needFlush = true;

        if (!same || this.camera != camera) {
            needFlush = false;
            if (rendererType == RendererType.SPRITE_BATCH)
                batch.setProjectionMatrix(camera.combined);
            else {
                if (same) {
                    shapeRenderer.flush();
                    batch.totalRenderCalls += 1;
                }
                shapeRenderer.setProjectionMatrix(camera.combined);
            }
        }

        if (crop != null) {
            if (crop != this.crop || this.camera != camera) {
                if (needFlush) {
                    if (rendererType == RendererType.SPRITE_BATCH) {
                        batch.flush();
                    } else {
                        shapeRenderer.flush();
                        batch.totalRenderCalls += 1;
                    }
                }
                Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
                projection.set(crop.x, crop.y, 0f);
                camera.project(projection);
                Gdx.gl.glScissor(
                    (int) projection.x,
                    (int) projection.y,
                    (int) (crop.width / camera.zoom),
                    (int) (crop.height / camera.zoom)
                );
            }
        } else if (this.crop != null) {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }

        this.crop = crop;
        this.camera = camera;
        this.rendererType = rendererType;

        if (same) return;

        diffs += 1;

        if (rendererType == RendererType.SPRITE_BATCH) {
            batch.enableBlending();
            batch.begin();
        } else {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            ShapeRenderer.ShapeType shapeType;
            switch (rendererType) {
                case SHAPE_POINT:
                    shapeType = ShapeRenderer.ShapeType.Point;
                    break;
                case SHAPE_LINE:
                    shapeType = ShapeRenderer.ShapeType.Line;
                    break;
                case SHAPE_FILLED:
                    shapeType = ShapeRenderer.ShapeType.Filled;
                    break;
                default:
                    throw new RuntimeException("No such shape type: " + rendererType);
            }
            shapeRenderer.begin(shapeType);
        }
    }

    public void begin(OrthographicCamera camera, RendererType rendererType) {
        begin(camera, rendererType, null);
    }

    public void end(boolean endCrop) {
        switch (rendererType) {
            case NONE:
                return;
            case SPRITE_BATCH:
                batch.end();
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                batch.setColor(Color.WHITE);
                if (batch.getShader() != Shader.getDefaultShaderProgram())
                    batch.setShader(Shader.getDefaultShaderProgram());
                break;
            default:
                shapeRenderer.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
                batch.totalRenderCalls += 1;

        }

        if (endCrop && crop != null) {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }

        rendererType = RendererType.NONE;
    }

    public void end() {
        end(true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
