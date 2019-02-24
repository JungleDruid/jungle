package net.natruid.jungle.utils.ai

import com.artemis.ComponentMapper
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.systems.PathfinderSystem
import net.natruid.jungle.systems.UnitManageSystem
import net.natruid.jungle.utils.Area

abstract class BehaviorLeaf : BehaviorNode() {
    protected lateinit var pathfinderSystem: PathfinderSystem
    protected lateinit var unitManageSystem: UnitManageSystem
    protected lateinit var mUnit: ComponentMapper<UnitComponent>

    protected val targets get() = mBehavior[self].targets

    protected fun getFullMoveArea(): Area {
        var moveArea = mBehavior[self].fullMoveArea
        if (moveArea == null) {
            moveArea = pathfinderSystem.area(mUnit[self].tile, null)
            mBehavior[self].fullMoveArea = moveArea
        }
        return moveArea
    }

    protected fun getMoveArea(): Area {
        var moveArea = mBehavior[self].moveArea
        if (moveArea == null) {
            moveArea = pathfinderSystem.area(mUnit[self].tile, unitManageSystem.getMovement(self))
            mBehavior[self].moveArea = moveArea
        }
        return moveArea
    }
}
