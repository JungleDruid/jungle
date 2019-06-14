package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.ComponentMapper
import net.natruid.jungle.components.render.CircleComponent
import net.natruid.jungle.utils.types.RendererType

class CircleRenderSystem : RenderSystem(
    Aspect.all(CircleComponent::class.java),
    RendererType.SHAPE_FILLED
) {
    private lateinit var mCircle: ComponentMapper<CircleComponent>

    override fun render(entityId: Int) {
        val shapeRenderer = renderer.shapeRenderer
        val circle = mCircle[entityId]

        shapeRenderer.color = circle.color
        val pos = getPos()
        shapeRenderer.circle(
            pos.x,
            pos.y,
            circle.radius,
            1.coerceAtLeast((8 * Math.cbrt(circle.radius.toDouble()) / camera.zoom).toInt())
        )
    }
}
