package net.natruid.jungle.utils.ai.actions

import net.natruid.jungle.utils.ai.BehaviorAction

class OverrideBehaviorAction(private val behaviorName: String) : BehaviorAction() {
    override fun evaluate(): Float? {
        return 0f
    }

    override fun execute(): Boolean {
        mBehavior[self].tree.overrideBehavior = behaviorName
        return true
    }

    override fun reset() {
    }
}
