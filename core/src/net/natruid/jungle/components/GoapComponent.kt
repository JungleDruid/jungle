package net.natruid.jungle.components

import com.artemis.PooledComponent
import net.natruid.jungle.utils.ai.GoapAction
import net.natruid.jungle.utils.ai.GoapType
import java.util.*

class GoapComponent : PooledComponent() {
    val availableActions = HashSet<GoapAction>()
    val currentActions = LinkedList<GoapAction>()
    val state = HashMap<GoapType, Boolean>()

    override fun reset() {
        availableActions.clear()
        currentActions.clear()
        state.clear()
    }
}
