package net.natruid.jungle.utils.ai

import com.artemis.World

abstract class BehaviorBranch : BehaviorNode() {
    val children = ArrayList<BehaviorNode>()

    fun addBehaviors(vararg nodes: BehaviorNode) {
        children.addAll(nodes)
    }

    override fun init(world: World, self: Int) {
        super.init(world, self)
        children.forEach {
            it.init(world, self)
        }
    }

    override fun reset() {
        children.forEach {
            it.reset()
        }
    }
}
