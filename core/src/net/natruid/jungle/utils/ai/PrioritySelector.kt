package net.natruid.jungle.utils.ai

open class PrioritySelector : BehaviorBranch() {
    override fun run(): Boolean {
        for (node in children) {
            if (node.run()) {
                return true
            }
        }
        return false
    }
}
