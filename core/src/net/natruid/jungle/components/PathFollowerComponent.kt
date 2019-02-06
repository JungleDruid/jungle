package net.natruid.jungle.components

import com.artemis.Component

class PathFollowerComponent : Component() {
    var path: Array<TileComponent>? = null
    var index = 0
}
