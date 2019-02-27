package net.natruid.jungle.utils.ai

class BehaviorTree : BehaviorBranch() {
    var overrideBehavior: String = ""

    override fun run(): Boolean {
        if (overrideBehavior.isNotEmpty()) {
            for (child in children) {
                if (child.name == overrideBehavior) return child.run()
            }
        }
        var hasOne = false
        for (child in children) {
            if (child.run()) hasOne = true
        }
        return hasOne
    }

    override fun reset() {
        if (self < 0) return
        mBehavior[self].let {
            it.targets.clear()
            it.moveArea = null
            it.fullMoveArea = null
            it.score = 0f
            it.execution = null
        }
        super.reset()
    }
}