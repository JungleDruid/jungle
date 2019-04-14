package net.natruid.jungle.utils.ai.conditions

import net.natruid.jungle.utils.ai.BehaviorCondition

class IsOverrideBehaviorCondition(private val behaviorName: String) : BehaviorCondition(false) {
    override fun reset() {
    }

    override fun run(): Boolean {
        return mBehavior[self].tree.overrideBehavior == behaviorName
    }
}
