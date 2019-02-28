package net.natruid.jungle.utils.ai.conditions

import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.utils.UnitCondition
import net.natruid.jungle.utils.UnitTargetType
import net.natruid.jungle.utils.ai.BehaviorCondition

class HasUnitInAttackRangeCondition(
    private val plusMovement: Boolean,
    private val preserveAp: Int = 0,
    saveResult: Boolean = true
) : BehaviorCondition(saveResult) {
    private lateinit var tileSystem: TileSystem

    override fun run(): Boolean {
        val attackRange = 1f
        val attackCost = 2
        val movement = if (plusMovement) {
            unitManageSystem.getMovement(self, attackCost + preserveAp)
        } else {
            0f
        }
        val targets = behaviorSystem.getSortedUnitList(self, UnitTargetType.HOSTILE, UnitCondition.CLOSE)
        val maxDistance = attackRange + movement
        targets.removeIf { mUnit[it] == null || tileSystem.getDistance(mUnit[self].tile, mUnit[it].tile) > maxDistance }
        targets.removeIf { unitManageSystem.getMoveAndAttackPath(self, it) == null }
        if (saveResult) mBehavior[self].targets.addAll(targets)
        return targets.isNotEmpty()
    }

    override fun reset() {
    }
}
