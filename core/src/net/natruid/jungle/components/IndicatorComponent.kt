package net.natruid.jungle.components

import com.artemis.Component
import com.artemis.annotations.EntityId

class IndicatorComponent(
    @EntityId var entityId: Int = -1,
    var type: IndicatorType = IndicatorType.MOVE_AREA
) : Component() {
    enum class IndicatorType { MOVE_AREA, MOVE_PATH }
}
