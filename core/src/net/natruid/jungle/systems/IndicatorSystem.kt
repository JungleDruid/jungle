package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.EntitySubscription
import com.artemis.utils.IntBag
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.components.IndicatorComponent.IndicatorType
import net.natruid.jungle.systems.TileSystem.Companion.tileSize
import net.natruid.jungle.utils.PathNode
import net.natruid.jungle.utils.Point
import net.natruid.jungle.utils.extensions.forEach
import java.text.DecimalFormat
import java.util.*

class IndicatorSystem : BaseEntitySystem(Aspect.all(
    IndicatorComponent::class.java,
    TransformComponent::class.java
).one(
    RectComponent::class.java,
    TextureComponent::class.java
)) {
    companion object {
        private val formatter = DecimalFormat("#.#")
    }

    private lateinit var sPathfinder: PathfinderSystem
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mRect: ComponentMapper<RectComponent>
    private lateinit var mLabel: ComponentMapper<LabelComponent>
    private lateinit var mIndicator: ComponentMapper<IndicatorComponent>
    private val moveAreaColor = Color(0f, 1f, 1f, .4f)
    private val resultMap = HashMap<Int, HashMap<IndicatorType, Array<PathNode>>>()

    override fun initialize() {
        super.initialize()
        world.aspectSubscriptionManager.get(Aspect.all())
            .addSubscriptionListener(object : EntitySubscription.SubscriptionListener {
                override fun inserted(entities: IntBag?) {}

                override fun removed(entities: IntBag?) {
                    entities?.forEach {
                        resultMap.remove(it)
                    }
                }
            })
    }

    fun addResult(entityId: Int, indicatorType: IndicatorType, pathfinderResult: Array<PathNode>) {
        var map = resultMap[entityId]
        if (map == null) {
            map = HashMap()
            resultMap[entityId] = map
        }

        map[indicatorType] = pathfinderResult
    }

    fun hasResult(entityId: Int, indicatorType: IndicatorType): Boolean {
        return resultMap[entityId]?.containsKey(indicatorType) ?: false
    }

    fun show(entityId: Int, indicatorType: IndicatorType) {
        var shown = false
        subscription.entities.forEach {
            mIndicator[it].apply {
                if (this.entityId != entityId || type != indicatorType) return@forEach
                mTransform[it].visible = true
                shown = true
            }
        }

        if (shown) return

        val result = resultMap[entityId]?.get(indicatorType)
            ?: error("[Error] Indicator: Cannot find result in $entityId-$indicatorType")
        for (p in result) {
            p.tile.let { tile ->
                world.create().let { indicator ->
                    mTransform.create(indicator).position = mTransform[tile].position
                    mRect.create(indicator).apply {
                        width = TileSystem.tileSize.toFloat()
                        height = tileSize.toFloat()
                        type = ShapeRenderer.ShapeType.Filled
                        color = moveAreaColor
                    }
                    mLabel.create(indicator).apply {
                        text = formatter.format(p.cost)
                        align = Align.center
                        fontName = "big"
                    }
                    mIndicator.create(indicator).apply {
                        this.entityId = entityId
                        type = indicatorType
                    }
                }
            }
        }

    }

    fun hide(entityId: Int, indicatorType: IndicatorType) {
        subscription.entities.forEach {
            mIndicator[it].apply {
                if (this.entityId != entityId || type != indicatorType) return@forEach
                mTransform[it].visible = false
            }
        }
    }

    fun getPathTo(coord: Point, entityId: Int): Deque<Int>? {
        val result = resultMap[entityId]?.get(IndicatorType.MOVE_AREA) ?: return null
        return sPathfinder.extractPath(result.asIterable(), coord)
    }

    fun showPathTo(coord: Point, entityId: Int): Boolean {
        val path = getPathTo(coord, entityId) ?: return false
        for (tile in path) {
            world.create().let { indicator ->
                mTransform.create(indicator).position = mTransform[tile].position
                mRect.create(indicator).apply {
                    width = tileSize.toFloat()
                    height = tileSize.toFloat()
                    type = ShapeRenderer.ShapeType.Filled
                    color = moveAreaColor
                }
                mIndicator.create(indicator).apply {
                    this.entityId = entityId
                    type = IndicatorType.MOVE_PATH
                }
            }
        }
        return true
    }

    fun remove(entityId: Int, indicatorType: IndicatorType) {
        when (indicatorType) {
            IndicatorType.MOVE_AREA -> {
                resultMap[entityId]?.remove(indicatorType)
                remove(entityId, IndicatorType.MOVE_PATH)
            }
            else -> {
            }
        }
        subscription.entities.forEach {
            mIndicator[it].apply {
                if (this.entityId != entityId || type != indicatorType) return@forEach
                world.delete(it)
            }
        }
    }

    override fun processSystem() {}
}
