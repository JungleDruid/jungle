package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.components.AnimationComponent;
import net.natruid.jungle.components.StatsComponent;
import net.natruid.jungle.components.UnitComponent;
import net.natruid.jungle.components.render.PivotComponent;
import net.natruid.jungle.utils.types.RendererType;

public class UnitHpRenderSystem extends RenderSystem {
    private static final float outWidth = 60;
    private static final float outHeight = 4;

    @SuppressWarnings("unused")

    private ComponentMapper<UnitComponent> mUnit;
    private ComponentMapper<StatsComponent> mStats;
    private ComponentMapper<AnimationComponent> mAnimation;

    public UnitHpRenderSystem() {
        super(Aspect.all(UnitComponent.class, StatsComponent.class), RendererType.SHAPE_FILLED, 11);
    }

    @Override
    public void render(int entityId) {
        ShapeRenderer shapeRenderer = renderer.shapeRenderer;
        PivotComponent pivot = PivotComponent.DEFAULT;

        float originX = outWidth * pivot.xy.x;
        float originY = outHeight * pivot.xy.y + 26f;
        shapeRenderer.setColor(Color.RED);
        int e = mAnimation.has(entityId) ? mUnit.get(entityId).tile : entityId;
        Vector3 pos = getPos(-originX, -originY, e);
        shapeRenderer.rect(pos.x, pos.y, outWidth, outHeight);

        float hpRatio = mUnit.get(entityId).hp / (float) mStats.get(entityId).getHp();
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(pos.x, pos.y, outWidth * hpRatio, outHeight);
    }
}
