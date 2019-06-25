package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.components.render.AngleComponent;
import net.natruid.jungle.components.render.PivotComponent;
import net.natruid.jungle.components.render.RectComponent;
import net.natruid.jungle.components.render.ScaleComponent;
import net.natruid.jungle.utils.types.RendererType;

public class RectRenderSystem extends RenderSystem {
    private ComponentMapper<RectComponent> mRect;
    private ComponentMapper<PivotComponent> mPivot;
    private ComponentMapper<ScaleComponent> mScale;
    private ComponentMapper<AngleComponent> mAngle;

    public RectRenderSystem() {
        super(Aspect.all(RectComponent.class), RendererType.SHAPE_FILLED);
    }

    @Override
    public void render(int entityId) {
        ShapeRenderer shapeRenderer = renderer.shapeRenderer;
        RectComponent rect = mRect.get(entityId);
        PivotComponent pivot = mPivot.getSafe(entityId, PivotComponent.DEFAULT);
        ScaleComponent scale = mScale.getSafe(entityId, ScaleComponent.DEFAULT);
        AngleComponent angle = mAngle.getSafe(entityId, AngleComponent.NONE);

        float originX = rect.width * pivot.xy.x;
        float originY = rect.height * pivot.xy.y;

        shapeRenderer.setColor(rect.color);
        Vector3 pos = getPos(-originX, -originY);
        shapeRenderer.rect(
            pos.x,
            pos.y,
            originX,
            originY,
            rect.width,
            rect.height,
            scale.xy.x,
            scale.xy.y,
            angle.rotation
        );
    }
}
