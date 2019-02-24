package net.natruid.jungle.utils.ai

abstract class BehaviorAction : BehaviorLeaf() {
    override fun run(): Boolean {
        val score = evaluate() ?: return false
        val behaviorComponent = mBehavior[self]
        behaviorComponent.score += score
        behaviorComponent.execution.add(this)
        return true
    }

    abstract fun evaluate(): Float?

    abstract fun execute(): Boolean
}
