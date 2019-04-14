package net.natruid.jungle.utils.ai.actions

import net.natruid.jungle.utils.ai.BehaviorAction

class AddThreatFromTargetsAction : BehaviorAction() {
    override fun evaluate(): Float? {
        return 0f
    }

    override fun execute(): Boolean {
        for (target in targets) {
            mBehavior[self]!!.threatMap[target] = (mBehavior[self]!!.threatMap[target] ?: 0f) + 1f
        }
        return true
    }

    override fun reset() {
    }
}
