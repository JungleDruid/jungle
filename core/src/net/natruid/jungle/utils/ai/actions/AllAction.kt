package net.natruid.jungle.utils.ai.actions

import com.artemis.World
import net.natruid.jungle.utils.ai.BehaviorAction

class AllAction(private vararg val actions: BehaviorAction) : BehaviorAction() {
    override fun evaluate(): Float? {
        var score = 0f
        for (action in actions) {
            score += action.evaluate() ?: return null
        }
        return score
    }

    override fun execute(): Boolean {
        var result = false
        for (action in actions) {
            if (action.execute()) {
                result = true
            }
        }
        return result
    }

    override fun reset() {
    }

    override fun init(world: World, self: Int) {
        super.init(world, self)
        for (action in actions) {
            action.init(world, self)
        }
    }
}
