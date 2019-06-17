package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.components.render.*;
import net.natruid.jungle.systems.CameraSystem;
import net.natruid.jungle.utils.RendererHelper;
import net.natruid.jungle.utils.types.RendererType;

public abstract class RenderSystem extends BaseEntitySystem {
    final float zOverride;
    private final RendererType rendererType;
    private final Vector3 position = new Vector3();
    protected RendererHelper renderer;
    private RenderBatchSystem renderBatchSystem;
    private CameraSystem cameraSystem;
    private ComponentMapper<PosComponent> mPos;
    private ComponentMapper<UIComponent> mUI;
    private ComponentMapper<ShaderComponent> mShader;
    private ComponentMapper<CropComponent> mCrop;
    private int current = -1;

    RenderSystem(Aspect.Builder aspect, RendererType rendererType) {
        this(aspect, rendererType, Float.NaN);
    }

    RenderSystem(Aspect.Builder aspect, RendererType rendererType, float zOverride) {
        super(aspect.all(RenderComponent.class).exclude(InvisibleComponent.class));
        this.rendererType = rendererType;
        this.zOverride = zOverride;
    }

    @Override
    protected void initialize() {
        super.initialize();
        renderer = renderBatchSystem.renderer;
        setEnabled(false);
    }

    protected OrthographicCamera getCamera() {
        return mUI.has(current) ? cameraSystem.uiCamera : cameraSystem.camera;
    }

    private void renderBegin() {
        if (rendererType == RendererType.NONE) return;

        if (rendererType == RendererType.SPRITE_BATCH) {
            ShaderComponent shaderComponent = mShader.getSafe(current, ShaderComponent.DEFAULT);
            ShaderProgram program = shaderComponent.shader.getProgram();
            SpriteBatch batch = renderer.batch;
            if (batch.getShader() != program) {
                batch.setShader(program);
            }
            batch.setBlendFunction(shaderComponent.blendSrcFunc, shaderComponent.blendDstFunc);
        }

        CropComponent cropComponent = mCrop.get(current);
        Rectangle rect = cropComponent != null ? cropComponent.rect : null;
        renderer.begin(getCamera(), rendererType, rect);
    }

    protected Vector3 getPos() {
        return getPos(0, 0, current);
    }

    protected Vector3 getPos(float offsetX, float offsetY) {
        return getPos(offsetX, offsetY, current);
    }

    protected Vector3 getPos(float offsetX, float offsetY, int entityId) {
        Vector2 xy = mPos.getSafe(entityId, PosComponent.DEFAULT).xy;
        position.set(xy.x + offsetX, xy.y + offsetY, 0f);
        if (mUI.has(entityId)) {
            cameraSystem.camera.project(position);
        }
        return position;
    }

    public void process(int e) {
        current = e;
        renderBegin();
        render(e);
    }

    @Override
    protected void inserted(int entityId) {
        super.inserted(entityId);
        renderBatchSystem.registerAgent(entityId, this);
    }

    @Override
    protected void removed(int entityId) {
        renderBatchSystem.unregisterAgent(entityId, this);
        super.removed(entityId);
    }

    public abstract void render(int entityId);

    @Override
    protected void processSystem() {
    }
}
