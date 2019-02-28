package net.natruid.jungle.events

class UnitHealthChangedEvent : PooledEvent() {
    var unit = -1
    var source = -1
    var amount = 0
    var killed = false

    override fun reset() {
        unit = -1
        source = -1
        amount = 0
        killed = false
    }
}
