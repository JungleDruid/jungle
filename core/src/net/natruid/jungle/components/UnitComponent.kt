package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import net.natruid.jungle.utils.Point

class UnitComponent(
        var coord: Point = Point(),
        var speed: Float = 0f,
        var faction: Faction = Faction.NONE
) : Component {
    enum class Faction(val value: Int) { NONE(0), PLAYER(1.shl(0)), ENEMY(1.shl(1)) }
}