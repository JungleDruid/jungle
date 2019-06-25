package net.natruid.jungle.systems.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import net.natruid.jungle.components.render.CustomRenderComponent;
import net.natruid.jungle.utils.callbacks.RenderCallback;
import net.natruid.jungle.utils.types.RendererType;

public class CustomRenderSystem extends RenderSystem {
    private ComponentMapper<CustomRenderComponent> mCustomRender;

    public CustomRenderSystem() {
        super(Aspect.all(CustomRenderComponent.class), RendererType.NONE);
    }

    @Override
    public void render(int entityId) {
        RenderCallback renderCallback = mCustomRender.get(entityId).renderCallback;
        if (renderCallback != null) renderCallback.invoke(renderer);
    }
}
