package net.natruid.jungle.components

import com.artemis.Component
import java.util.*

class PathFollowerComponent : Component() {
    var path: Deque<Int>? = null
}
