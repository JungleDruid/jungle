package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import net.natruid.jungle.components.render.AngleComponent;
import net.natruid.jungle.components.render.PivotComponent;
import net.natruid.jungle.components.render.ScaleComponent;
import net.natruid.jungle.components.render.TextureComponent;
import net.natruid.jungle.utils.types.RendererType;

public class ImageRenderSystem extends RenderSystem {
    private ComponentMapper<TextureComponent> mTexture;
    private ComponentMapper<PivotComponent> mPivot;
    private ComponentMapper<ScaleComponent> mScale;
    private ComponentMapper<AngleComponent> mAngle;

    public ImageRenderSystem() {
        super(Aspect.all(TextureComponent.class), RendererType.SPRITE_BATCH);
    }

    @Override
    public void render(int entityId) {
        SpriteBatch batch = renderer.batch;
        TextureComponent texture = mTexture.get(entityId);
        TextureRegion region = texture.getRegion();
        if (region != null) {

            PivotComponent pivot = mPivot.getSafe(entityId, PivotComponent.DEFAULT);
            ScaleComponent scale = mScale.getSafe(entityId, ScaleComponent.DEFAULT);
            AngleComponent angle = mAngle.getSafe(entityId, AngleComponent.NONE);

            float width = region.getRegionWidth();
            int height = region.getRegionHeight();

            float originX = width * pivot.xy.x;
            float originY = height * pivot.xy.y;

            region.flip(region.isFlipX() != texture.flipX, region.isFlipY() != texture.flipY);
            batch.setColor(texture.color);
            Vector3 pos = getPos(-originX, -originY);
            batch.draw(
                region,
                pos.x,
                pos.y,
                originX,
                originY,
                width,
                height,
                scale.xy.x,
                scale.xy.y,
                angle.rotation
            );
        }
    }
}
