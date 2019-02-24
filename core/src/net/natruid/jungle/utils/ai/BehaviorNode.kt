package net.natruid.jungle.utils.ai

import com.artemis.ComponentMapper
import com.artemis.World
import net.natruid.jungle.components.BehaviorComponent
import net.natruid.jungle.systems.BehaviorSystem

abstract class BehaviorNode {
    protected lateinit var behaviorSystem: BehaviorSystem
    protected lateinit var mBehavior: ComponentMapper<BehaviorComponent>

    var name: String = ""

    protected var self: Int = -1
        private set

    open fun init(world: World, self: Int) {
        assert(self >= 0)
        this.self = self
        world.inject(this)
    }

    abstract fun reset()

    abstract fun run(): Boolean
}
