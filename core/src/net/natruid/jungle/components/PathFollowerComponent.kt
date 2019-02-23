package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.PathNode
import java.util.*

class PathFollowerComponent : Component() {
    var path: Deque<PathNode>? = null
    var callback: (() -> Unit)? = null
}
