package net.natruid.jungle.utils.ai

class BehaviorTree : PrioritySelector() {
    var overrideBehavior: String = ""

    override fun run(): Boolean {
        if (overrideBehavior.isNotEmpty()) {
            for (child in children) {
                if (child.name == overrideBehavior) return child.run()
            }
        }
        return super.run()
    }

    override fun reset() {
        if (self < 0) return
        mBehavior[self].let {
            it.targets.clear()
            it.moveArea = null
            it.fullMoveArea = null
            it.score = 0f
            it.execution.clear()
        }
        super.reset()
    }
}
