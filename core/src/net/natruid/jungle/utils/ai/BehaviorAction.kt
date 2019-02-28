package net.natruid.jungle.utils.ai

import net.mostlyoriginal.api.event.common.EventSystem

abstract class BehaviorAction : BehaviorLeaf() {
    protected lateinit var es: EventSystem

    override fun run(): Boolean {
        val score = evaluate() ?: return false
        val behaviorComponent = mBehavior[self]
        if (score > behaviorComponent.score || behaviorComponent.execution == null) {
            behaviorComponent.score = score
            behaviorComponent.execution = this
        }
        return true
    }

    abstract fun evaluate(): Float?

    abstract fun execute(): Boolean
}
