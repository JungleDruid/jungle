package net.natruid.jungle.components

import com.artemis.Component
import com.artemis.annotations.EntityId
import net.natruid.jungle.utils.Faction

class UnitComponent : Component() {
    @EntityId
    var tile: Int = -1
    var faction: Faction = Faction.NONE
    var level: Int = 0
    var exp: Int = 0
    var ap: Int = 0
    var extraMovement: Float = 0f
    var hasTurn: Boolean = false
}
