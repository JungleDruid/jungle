package net.natruid.jungle.utils.ai.actions

import net.natruid.jungle.events.UnitAttackEvent
import net.natruid.jungle.utils.Path
import net.natruid.jungle.utils.ai.BehaviorAction
import net.natruid.jungle.utils.extensions.dispatch

class AttackAction : BehaviorAction() {
    private var target = -1
    private var path: Path? = null

    override fun evaluate(): Float? {
        val attackCost = 2
        val targets = mBehavior[self].targets
        if (targets.isEmpty()) return null
        var bestTarget = -1
        var bestScore = Float.NaN
        for (target in targets) {
            val damage = unitManageSystem.getDamage(self, target)
            val kill = mUnit[target].hp < damage
            val score = behaviorSystem.getScore(if (kill) "kill" else "damage", attackCost, damage.toFloat())
            if (bestTarget == -1 || score > bestScore) {
                bestTarget = target
                bestScore = score
            }
        }
        if (bestTarget < 0) return null
        val path = unitManageSystem.getMoveAndActPath(self, bestTarget, 2, 1f) ?: error("No path")
        if (path.isEmpty()) error("Path is empty")
        target = bestTarget
        this.path = path
        return bestScore - path.last.cost / 100f
    }

    override fun execute(): Boolean {
        es.dispatch(UnitAttackEvent::class).let {
            it.unit = self
            it.target = target
            it.path = path!!
        }
        return true
    }

    override fun reset() {
        target = -1
        path = null
    }
}
