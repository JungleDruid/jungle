package net.natruid.jungle.components

import com.artemis.PooledComponent
import net.natruid.jungle.utils.Area
import net.natruid.jungle.utils.IndicatorType

class IndicatorOwnerComponent : PooledComponent() {
    val resultMap = HashMap<IndicatorType, Area>()
    val indicatorMap = HashMap<IndicatorType, IntArray>()
    override fun reset() {
        resultMap.clear()
        indicatorMap.clear()
    }
}
