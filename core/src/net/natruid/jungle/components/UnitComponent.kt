package net.natruid.jungle.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class UnitComponent : Component, Pool.Poolable {
    enum class Faction(val value: Int) { NONE(0), PLAYER(1.shl(0)), ENEMY(1.shl(1)) }

    var x = 0
    var y = 0
    var speed = 0f
    var faction = Faction.NONE

    override fun reset() {
        x = 0
        y = 0
        speed = 0f
        faction = Faction.NONE
    }
}