package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import net.natruid.jungle.components.AttributesComponent
import net.natruid.jungle.components.BehaviorComponent
import net.natruid.jungle.components.UnitComponent

class ThreatSystem : BaseEntitySystem(Aspect.all(BehaviorComponent::class.java)) {
    private lateinit var mBehavior: ComponentMapper<BehaviorComponent>
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mAttributes: ComponentMapper<AttributesComponent>
    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var tileSystem: TileSystem

    fun checkAlert(unit: Int, tile: Int = -1) {
        if (entityIds.isEmpty) return
        if (!mUnit.has(unit)) return
        val t = if (tile >= 0) tile else mUnit[unit].tile
        val data = entityIds.data
        for (i in 0 until entityIds.size()) {
            val u = data[i]
            if (!unitManageSystem.isEnemy(u, unit)) continue
            if (mBehavior[u].threatMap.isNotEmpty()) continue
            val maxDistance = 6f * 1f + (mAttributes[u].awareness - 10) * 0.05f
            if (tileSystem.getDistance(mUnit[u].tile, t) <= maxDistance) {
                mBehavior[u].threatMap[u] = 1f
            }
        }
    }

    override fun processSystem() {}
}
