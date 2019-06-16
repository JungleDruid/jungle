package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import net.natruid.jungle.components.LabelComponent;
import net.natruid.jungle.components.render.PivotComponent;
import net.natruid.jungle.core.Sky;
import net.natruid.jungle.utils.types.RendererType;

public class LabelRenderSystem extends RenderSystem {
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private ComponentMapper<LabelComponent> mLabel;
    private ComponentMapper<PivotComponent> mPivot;

    public LabelRenderSystem() {
        super(Aspect.all(LabelComponent.class), RendererType.SPRITE_BATCH);
    }

    @Override
    public void render(int entityId) {
        LabelComponent label = mLabel.get(entityId);
        PivotComponent pivot = mPivot.getSafe(entityId, PivotComponent.DEFAULT);

        BitmapFont font = Sky.marsh.font.get(label.fontName);
        int hAlign = label.align == Align.top ? Align.center
            : label.align == Align.bottom ? Align.center
            : label.align;
        glyphLayout.setText(font, label.text, label.color, label.width, hAlign, label.width > 0f);
        float offsetY = (label.align & Align.top) != 0 ? 0f
            : (label.align & Align.bottom) != 0 ? glyphLayout.height
            : glyphLayout.height * pivot.xy.y;
        Vector3 position = getPos();
        font.draw(renderer.batch, glyphLayout, position.x, position.y + offsetY);
    }
}
