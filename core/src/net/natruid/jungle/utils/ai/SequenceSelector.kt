package net.natruid.jungle.utils.ai

class SequenceSelector : BehaviorBranch() {
    override fun run(): Boolean {
        for (node in children) {
            if (!node.run()) {
                return false
            }
        }
        return true
    }
}
