package net.natruid.jungle.components

import com.artemis.PooledComponent
import net.natruid.jungle.utils.IndicatorType
import net.natruid.jungle.utils.PathNode

class IndicatorOwnerComponent : PooledComponent() {
    val resultMap = HashMap<IndicatorType, Array<PathNode>>()
    val indicatorMap = HashMap<IndicatorType, IntArray>()
    override fun reset() {
        resultMap.clear()
        indicatorMap.clear()
    }
}
