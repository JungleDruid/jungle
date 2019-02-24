package net.natruid.jungle.utils.ai.actions

import com.artemis.ComponentMapper
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.utils.Path
import net.natruid.jungle.utils.ai.BehaviorAction

class MoveTowardUnitAction(private val preserveAp: Int = 0) : BehaviorAction() {
    private lateinit var mTile: ComponentMapper<TileComponent>

    private var path: Path? = null

    override fun evaluate(): Float? {
        val target = targets.first()
        assert(target >= 0)
        val area = getFullMoveArea()
        val maxMovement = unitManageSystem.getMovement(self, preserveAp)
        val path = pathfinderSystem.extractPath(area, mUnit[target].tile, true) ?: return null
        if (path.isEmpty()) return null
        val cost = path.last.cost
        while (path.isNotEmpty() && (path.last.cost > maxMovement || mTile[path.last.tile]!!.unit >= 0)) {
            path.removeLast()
        }
        this.path = path
        if (path.isEmpty()) return null
        return path.last.cost - cost
    }

    override fun execute(): Boolean {
        unitManageSystem.moveUnit(self, path!!)
        return true
    }

    override fun reset() {
        path = null
    }
}
