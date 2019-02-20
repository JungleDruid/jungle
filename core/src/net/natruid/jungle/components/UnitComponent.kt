package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.Point

class UnitComponent : Component() {
    var coord: Point = Point()
    var faction: Faction = Faction.NONE
    var level: Int = 0
    var exp: Int = 0
    var ap: Int = 0
    var extraMovement: Float = 0f
    var hasTurn: Boolean = false
}
