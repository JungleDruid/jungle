package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.Point

class UnitComponent : Component() {
    var coord: Point = Point()
    var speed: Float = 0f
    var faction: Faction = Faction.NONE

    enum class Faction(val value: Int) { NONE(0), PLAYER(1.shl(0)), ENEMY(1.shl(1)) }
}
