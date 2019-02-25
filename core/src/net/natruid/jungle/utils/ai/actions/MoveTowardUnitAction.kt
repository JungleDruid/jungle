package net.natruid.jungle.utils.ai.actions

import net.natruid.jungle.systems.TileSystem
import net.natruid.jungle.utils.ExtractPathType
import net.natruid.jungle.utils.Path
import net.natruid.jungle.utils.ai.BehaviorAction

class MoveTowardUnitAction(private val preserveAp: Int = 0) : BehaviorAction() {
    private lateinit var tileSystem: TileSystem

    private var path: Path? = null

    override fun evaluate(): Float? {
        val target = targets.first()
        assert(target >= 0)
        val maxMovement = unitManageSystem.getMovement(self, preserveAp)
        val path = pathfinderSystem.path(
            mUnit[self].tile,
            mUnit[target].tile,
            unit = self,
            type = ExtractPathType.CLOSEST,
            maxCost = maxMovement
        )
        if (path == null || path.isEmpty()) {
//            Logger.debug { "$self cannot find path" }
            return null
        }
        if (path.last.tile == mUnit[self].tile) {
//            Logger.debug { "$self is already at the destination" }
            return null
        }
        val newDist = tileSystem.getDistance(mUnit[target].tile, path.last.tile)
        this.path = path
        return behaviorSystem.getScore(
            "move forward",
            unitManageSystem.getMovementCost(self, path.last.cost, true),
            -newDist
        )
    }

    override fun execute(): Boolean {
//        Logger.debug { "$self moving with path size: ${path!!.size}" }
        unitManageSystem.moveUnit(self, path!!)
        mBehavior[self].fullMoveArea = null
        return true
    }

    override fun reset() {
        path = null
    }
}
