package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.ComponentMapper
import net.natruid.jungle.components.render.CustomRenderComponent
import net.natruid.jungle.utils.types.RendererType

class CustomRenderSystem : RenderSystem(
    Aspect.all(CustomRenderComponent::class.java),
    RendererType.NONE
) {
    private lateinit var mCustomRender: ComponentMapper<CustomRenderComponent>

    override fun render(entityId: Int) {
        mCustomRender[entityId].renderCallback?.invoke(renderer)
    }
}
