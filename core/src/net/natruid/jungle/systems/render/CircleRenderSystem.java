package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.components.render.CircleComponent;
import net.natruid.jungle.utils.types.RendererType;

public class CircleRenderSystem extends RenderSystem {
    private ComponentMapper<CircleComponent> mCircle;

    public CircleRenderSystem() {
        super(Aspect.all(CircleComponent.class), RendererType.SHAPE_FILLED);
    }

    @Override
    public void render(int entityId) {
        ShapeRenderer shapeRenderer = renderer.shapeRenderer;
        CircleComponent circle = mCircle.get(entityId);

        shapeRenderer.setColor(circle.color);
        Vector3 pos = getPos();
        shapeRenderer.circle(
            pos.x,
            pos.y,
            circle.radius,
            Math.max((int) (8 * Math.cbrt(circle.radius) / getCamera().zoom), 1)
        );
    }
}
