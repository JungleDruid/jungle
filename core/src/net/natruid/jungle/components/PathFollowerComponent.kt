package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.Path

class PathFollowerComponent : Component() {
    var path: Path? = null
    var callback: (() -> Unit)? = null
}
