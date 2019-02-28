package net.natruid.jungle.events

import net.natruid.jungle.utils.Path

class UnitMoveEvent : PooledEvent() {
    var unit = -1
    var targetTile = -1
    var path: Path? = null
    var free = false

    override fun reset() {
        unit = -1
        targetTile = -1
        path = null
        free = false
    }
}
