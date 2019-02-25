package net.natruid.jungle.systems

import com.artemis.BaseSystem
import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.utils.Bag
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import net.natruid.jungle.components.*
import net.natruid.jungle.systems.TileSystem.Companion.tileSize
import net.natruid.jungle.utils.*
import java.text.DecimalFormat

class IndicateSystem : BaseSystem() {
    companion object {
        private val formatter = DecimalFormat("#.#")
    }

    private lateinit var pathfinderSystem: PathfinderSystem
    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mRect: ComponentMapper<RectComponent>
    private lateinit var mLabel: ComponentMapper<LabelComponent>
    private lateinit var mIndicator: ComponentMapper<IndicatorComponent>
    private lateinit var mIndicatorOwner: ComponentMapper<IndicatorOwnerComponent>
    private lateinit var mUI: ComponentMapper<UIComponent>
    private val moveAreaColor = Color(0f, 1f, 1f, .4f)

    fun addResult(entityId: Int, indicatorType: IndicatorType, pathfinderResult: Area) {
        val cOwner = mIndicatorOwner[entityId] ?: mIndicatorOwner.create(entityId)

        cOwner.resultMap[indicatorType] = pathfinderResult
    }

    fun hasResult(entityId: Int, indicatorType: IndicatorType): Boolean {
        return mIndicatorOwner[entityId]?.resultMap?.containsKey(indicatorType) ?: false
    }

    private val entityArrayBuilder = ArrayList<Int>()
    fun show(entityId: Int, indicatorType: IndicatorType) {
        var shown = false

        val cOwner = mIndicatorOwner[entityId] ?: error("Cannot find indicator owner: $entityId")
        cOwner.indicatorMap[indicatorType]?.forEach {
            mTransform[it]?.visible = true
        }?.apply { shown = true }

        if (shown) return

        val result = cOwner.resultMap[indicatorType] ?: error("Cannot find indicator result: $entityId-$indicatorType")
        for (p in result) {
            p.tile.let { tile ->
                world.create().let { indicator ->
                    mTransform.create(indicator).apply {
                        position = mTransform[tile].position
                        z = Constants.Z_PATH_INDICATOR
                    }
                    mRect.create(indicator).apply {
                        width = TileSystem.tileSize.toFloat()
                        height = tileSize.toFloat()
                        type = ShapeRenderer.ShapeType.Filled
                        color = moveAreaColor
                    }
                    mIndicator.create(indicator).apply {
                        this.entityId = entityId
                        type = indicatorType
                    }
                    entityArrayBuilder.add(indicator)
                }
                world.create().let { indicatorText ->
                    mTransform.create(indicatorText).apply {
                        position = mTransform[tile].position
                    }
                    mLabel.create(indicatorText).apply {
                        text = formatter.format(p.cost)
                        align = Align.center
                        fontName = "small"
                    }
                    mIndicator.create(indicatorText).apply {
                        this.entityId = entityId
                        type = indicatorType
                    }
                    mUI.create(indicatorText)
                    entityArrayBuilder.add(indicatorText)
                }
            }
        }
        cOwner.indicatorMap[indicatorType] = entityArrayBuilder.toIntArray()
        entityArrayBuilder.clear()
    }

    fun hide(entityId: Int, indicatorType: IndicatorType) {
        mIndicatorOwner[entityId]?.indicatorMap?.get(indicatorType)?.forEach {
            if (!mIndicator.has(it)) {
                Logger.warn { "Entity $it doesn't have indicator component." }
                val bag = Bag<Component>()
                world.componentManager.getComponentsFor(it, bag)
                var first = true
                bag.forEach { c ->
                    if (!first) print(", ")
                    print(c::class.simpleName?.replace("Component", ""))
                    first = false
                }
                println()
            } else {
                mTransform[it]?.visible = false
            }
        }
    }

    fun getPathTo(goal: Int, entityId: Int): Path? {
        val result = mIndicatorOwner[entityId]?.resultMap?.get(IndicatorType.MOVE_AREA) ?: return null
        return pathfinderSystem.extractPath(result, goal)
    }

    fun showPathTo(goal: Int, entityId: Int): Float {
        val path = getPathTo(goal, entityId) ?: return Float.MIN_VALUE
        remove(entityId, IndicatorType.MOVE_PATH)
        for (node in path) {
            world.create().let { indicator ->
                mTransform.create(indicator).apply {
                    position = mTransform[node.tile].position
                    z = Constants.Z_PATH_INDICATOR
                }
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
                entityArrayBuilder.add(indicator)
            }
        }
        mIndicatorOwner[entityId].indicatorMap[IndicatorType.MOVE_PATH] = entityArrayBuilder.toIntArray()
        entityArrayBuilder.clear()
        return path.last.cost
    }

    fun remove(entityId: Int, indicatorType: IndicatorType) {
        val cOwner = mIndicatorOwner[entityId] ?: return
        when (indicatorType) {
            IndicatorType.MOVE_AREA -> {
                cOwner.resultMap.remove(indicatorType)
                remove(entityId, IndicatorType.MOVE_PATH)
            }
            else -> {
            }
        }
        cOwner.indicatorMap[indicatorType]?.forEach {
            if (mIndicator.has(it)) world.delete(it)
        }
        cOwner.indicatorMap.remove(indicatorType)
    }

    override fun processSystem() {}
}
