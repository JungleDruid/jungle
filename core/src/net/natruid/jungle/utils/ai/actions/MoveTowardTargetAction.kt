package net.natruid.jungle.utils.ai.actions

import com.artemis.ComponentMapper
import net.natruid.jungle.components.TileComponent
import net.natruid.jungle.systems.PathfinderSystem
import net.natruid.jungle.systems.UnitManageSystem
import net.natruid.jungle.utils.PathNode
import net.natruid.jungle.utils.ai.GoapAction
import net.natruid.jungle.utils.ai.GoapType
import java.util.*

class MoveTowardTargetAction : GoapAction() {
    private lateinit var pathfinderSystem: PathfinderSystem
    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var mTile: ComponentMapper<TileComponent>

    private val cache = HashMap<Int, Deque<PathNode>?>()

    override fun prepare() {
        addPrecondition(GoapType.HAS_ENEMY, true)
        addEffect(GoapType.CLOSER_TO_ENEMY, true)
    }

    override fun check(self: Int): Float? {
        if (cache.containsKey(self)) return cache[self]?.last?.cost

        var path: Deque<PathNode>? = null
        var cost = Float.MAX_VALUE
        for (enemy in goapSystem.enemies!!) {
            val path1 = pathfinderSystem.path(mUnit[self].tile, mUnit[enemy].tile) ?: continue
            if (cost > path1.last.cost) {
                path = path1
                cost = path1.last.cost
            } else {
                continue
            }
        }
        if (path == null) return null
        path.removeLast()
        if (path.isNotEmpty()) {
            val movement = unitManageSystem.getMovement(self)
            while (path.isNotEmpty() && (path.last.cost > movement || mTile[path.last.tile]?.unit != -1)) {
                path.removeLast()
            }
            cache[self] = path
            if (path.isEmpty()) return null
            return path.last.cost
        }
        return null
    }

    override fun perform(self: Int) {
        unitManageSystem.moveUnit(self, cache[self]!!)
    }

    override fun reset() {
        cache.clear()
    }
}
