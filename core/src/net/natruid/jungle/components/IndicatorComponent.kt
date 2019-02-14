package net.natruid.jungle.components

import com.artemis.Component
import com.artemis.annotations.EntityId
import net.natruid.jungle.utils.IndicatorType

class IndicatorComponent(
    @EntityId var entityId: Int = -1,
    var type: IndicatorType = IndicatorType.MOVE_AREA
) : Component()
