package net.natruid.jungle.events

import net.natruid.jungle.utils.Path

class UnitAttackEvent : PooledEvent() {
    var unit = -1
    var target = -1
    var path: Path? = null

    override fun reset() {
        unit = -1
        target = -1
        path = null
    }
}
