package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.ComponentMapper
import net.natruid.jungle.components.render.AngleComponent
import net.natruid.jungle.components.render.PivotComponent
import net.natruid.jungle.components.render.RectComponent
import net.natruid.jungle.components.render.ScaleComponent
import net.natruid.jungle.utils.types.RendererType

class RectRenderSystem : RenderSystem(
    Aspect.all(RectComponent::class.java),
    RendererType.SHAPE_FILLED
) {
    private lateinit var mRect: ComponentMapper<RectComponent>
    private lateinit var mPivot: ComponentMapper<PivotComponent>
    private lateinit var mScale: ComponentMapper<ScaleComponent>
    private lateinit var mAngle: ComponentMapper<AngleComponent>

    override fun render(entityId: Int) {
        val shapeRenderer = renderer.shapeRenderer
        val rect = mRect[entityId]
        val pivot = mPivot.getSafe(entityId, PivotComponent.DEFAULT)
        val scale = mScale.getSafe(entityId, ScaleComponent.DEFAULT)
        val angle = mAngle.getSafe(entityId, AngleComponent.NONE)

        val originX = rect.width * pivot.xy.x
        val originY = rect.height * pivot.xy.y

        shapeRenderer.color = rect.color
        val pos = getPos(-originX, -originY)
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
        )
    }
}
