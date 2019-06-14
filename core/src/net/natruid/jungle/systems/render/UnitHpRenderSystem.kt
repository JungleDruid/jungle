package net.natruid.jungle.systems.render

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.badlogic.gdx.graphics.Color
import net.natruid.jungle.components.AnimationComponent
import net.natruid.jungle.components.StatsComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.components.render.PivotComponent
import net.natruid.jungle.systems.UnitManageSystem
import net.natruid.jungle.utils.types.RendererType

class UnitHpRenderSystem : RenderSystem(
    Aspect.all(
        UnitComponent::class.java,
        StatsComponent::class.java
    ),
    RendererType.SHAPE_FILLED
) {
    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mStats: ComponentMapper<StatsComponent>
    private lateinit var mAnimation: ComponentMapper<AnimationComponent>

    private val outWidth = 60f
    private val outHeight = 4f

    override val zOverride = 11f

    override fun render(entityId: Int) {
        val shapeRenderer = renderer.shapeRenderer
        val pivot = PivotComponent.DEFAULT

        val originX = outWidth * pivot.xy.x
        val originY = outHeight * pivot.xy.y + 26f
        shapeRenderer.color = Color.RED
        val e = if (mAnimation.has(entityId)) mUnit[entityId].tile else entityId
        val pos = getPos(-originX, -originY, e)
        shapeRenderer.rect(pos.x, pos.y, outWidth, outHeight)

        val hpRatio = mUnit[entityId].hp / mStats[entityId].hp.toFloat()
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(pos.x, pos.y, outWidth * hpRatio, outHeight)
    }
}
