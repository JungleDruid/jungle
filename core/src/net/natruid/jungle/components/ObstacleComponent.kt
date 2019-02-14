package net.natruid.jungle.components

import com.artemis.Component
import net.natruid.jungle.utils.ObstacleType

class ObstacleComponent(
    var type: ObstacleType = ObstacleType.TREE,
    var destroyable: Boolean = false,
    var maxHp: Float = 0f,
    var hp: Float = 0f
) : Component()
