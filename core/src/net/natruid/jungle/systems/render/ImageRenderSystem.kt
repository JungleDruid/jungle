package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.ComponentMapper
import net.natruid.jungle.components.render.AngleComponent
import net.natruid.jungle.components.render.PivotComponent
import net.natruid.jungle.components.render.ScaleComponent
import net.natruid.jungle.components.render.TextureComponent

class ImageRenderSystem : RenderSystem(
    Aspect.all(TextureComponent::class.java)
) {
    private lateinit var mTexture: ComponentMapper<TextureComponent>
    private lateinit var mPivot: ComponentMapper<PivotComponent>
    private lateinit var mScale: ComponentMapper<ScaleComponent>
    private lateinit var mAngle: ComponentMapper<AngleComponent>

    override fun render(entityId: Int) {
        val batch = renderer.batch
        val texture = mTexture[entityId]
        texture.region?.let { region ->
            val pivot = mPivot.getSafe(entityId, PivotComponent.DEFAULT)
            val scale = mScale.getSafe(entityId, ScaleComponent.DEFAULT)
            val angle = mAngle.getSafe(entityId, AngleComponent.NONE)

            val width = region.regionWidth.toFloat()
            val height = region.regionHeight.toFloat()

            val originX = width * pivot.xy.x
            val originY = height * pivot.xy.y

            region.flip(region.isFlipX != texture.flipX, region.isFlipY != texture.flipY)
            batch.color = texture.color
            val pos = getPos(-originX, -originY)
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
            )
        }
    }
}
