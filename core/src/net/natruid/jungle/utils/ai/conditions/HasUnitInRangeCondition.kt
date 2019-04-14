package net.natruid.jungle.utils.ai.conditions

import com.artemis.ComponentMapper
import net.natruid.jungle.components.AttributesComponent
import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.utils.UnitTargetType
import net.natruid.jungle.utils.ai.BehaviorCondition

class HasUnitInRangeCondition(
    private val range: Float,
    private val group: UnitTargetType,
    private val awarenessMod: Boolean,
    saveResult: Boolean
) : BehaviorCondition(saveResult) {
    private lateinit var mAttributes: ComponentMapper<AttributesComponent>
    private lateinit var tileSystem: TileSystem

    override fun reset() {
    }

    override fun run(): Boolean {
        val targets = behaviorSystem.getUnitGroup(group)
        val maxDistance = range * if (!awarenessMod) 1f else 1f + (mAttributes[self].awareness - 10) * 0.05f
        var result = false
        for (target in targets) {
            if (mUnit[target] != null) {
                if (tileSystem.getDistance(mUnit[self].tile, mUnit[target].tile) <= maxDistance) {
                    if (saveResult) {
                        result = true
                        mBehavior[self].targets.add(target)
                    } else {
                        return true
                    }
                }
            }
        }
        return result
    }
}
