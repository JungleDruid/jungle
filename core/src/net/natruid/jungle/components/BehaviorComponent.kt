package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.Area
import net.natruid.jungle.utils.ai.BehaviorAction
import net.natruid.jungle.utils.ai.BehaviorTree
import java.util.*

class BehaviorComponent : Component() {
    lateinit var tree: BehaviorTree
    val targets = ArrayList<Int>()
    var moveArea: Area? = null
    var fullMoveArea: Area? = null
    val execution = LinkedList<BehaviorAction>()
    var score: Float = 0f
}
