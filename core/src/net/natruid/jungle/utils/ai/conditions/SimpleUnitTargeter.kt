package net.natruid.jungle.utils.ai.conditions

import net.natruid.jungle.utils.UnitCondition
import net.natruid.jungle.utils.UnitTargetType
import net.natruid.jungle.utils.ai.BehaviorCondition

class SimpleUnitTargeter(
    private val targetType: UnitTargetType,
    private val condition: UnitCondition,
    saveResult: Boolean = true
) : BehaviorCondition(saveResult) {
    override fun run(): Boolean {
        val target = behaviorSystem.getUnit(self, targetType, condition)
        if (target < 0) return false
        if (saveResult) targets.add(target)
        return true
    }

    override fun reset() {}
}
