package net.natruid.jungle.components

import com.artemis.PooledComponent
import com.artemis.annotations.EntityId
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.skill.Skill

class UnitComponent : PooledComponent() {
    @EntityId
    var tile: Int = -1
    var faction: Faction = Faction.NONE
    var level: Int = 0
    var exp: Int = 0
    var hp: Int = 0
    var ap: Int = 0
    var extraMovement: Float = 0f
    var hasTurn: Boolean = false
    val skills: ArrayList<Skill> = ArrayList()

    override fun reset() {
        tile = -1
        faction = Faction.NONE
        level = 0
        exp = 0
        hp = 0
        ap = 0
        extraMovement = 0f
        hasTurn = false
        skills.clear()
    }
}
