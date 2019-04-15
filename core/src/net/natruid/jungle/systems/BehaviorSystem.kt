package net.natruid.jungle.systems

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ktx.async.KtxAsync
import ktx.async.skipFrame
import net.natruid.jungle.components.AttributesComponent
import net.natruid.jungle.components.BehaviorComponent
import net.natruid.jungle.components.UnitComponent
import net.natruid.jungle.utils.Faction
import net.natruid.jungle.utils.UnitCondition
import net.natruid.jungle.utils.UnitTargetType

class BehaviorSystem : BaseEntitySystem(Aspect.all(BehaviorComponent::class.java)) {
    private enum class Phase { READY, CHECKING, PLANNING, PERFORMING, STOPPING, STOPPED }

    private lateinit var unitManageSystem: UnitManageSystem
    private lateinit var combatTurnSystem: CombatTurnSystem
    private lateinit var animateSystem: AnimateSystem
    private lateinit var tileSystem: TileSystem
    private lateinit var mUnit: ComponentMapper<UnitComponent>
    private lateinit var mBehavior: ComponentMapper<BehaviorComponent>
    private lateinit var mAttributes: ComponentMapper<AttributesComponent>

    private val unitGroup = HashMap<UnitTargetType, List<Int>>()
    private var phase = Phase.STOPPED
    private var currentJob: Job? = null
    private var planJob: Job? = null
    private var currentUnit = -1
    private var performingUnit = -1
    private val scoreMap = mapOf(Pair("kill", 1000f), Pair("damage", 100f))
    private val activeUnits = HashSet<Int>()
    private val idleUnits = HashSet<Int>()

    fun prepare() {
        if (phase == Phase.READY) return
        if (phase != Phase.STOPPED) error("GoapSystem has not stopped yet: $phase")
        phase = Phase.READY
    }

    fun reset() {
        currentJob?.apply {
            cancel()
            currentJob = null
        }
        planJob?.apply {
            cancel()
            planJob = null
        }
        currentUnit = -1
        unitGroup.clear()
        phase = Phase.STOPPED
    }

    private inline fun loopAgents(idle: Boolean = false, function: (id: Int) -> Boolean) {
        val currentFaction = combatTurnSystem.faction
        val set = if (idle) idleUnits else activeUnits
        for (unit in set) {
            if (mUnit[unit].faction != currentFaction) continue
            if (function(unit)) break
        }
    }

    override fun inserted(entityId: Int) {
        super.inserted(entityId)
        mBehavior[entityId].tree.init(world, entityId)
        idleUnits.add(entityId)
    }

    override fun removed(entityId: Int) {
        super.removed(entityId)
        idleUnits.remove(entityId)
        activeUnits.remove(entityId)
    }

    override fun processSystem() {
        when (phase) {
            Phase.READY -> {
                if (combatTurnSystem.faction == Faction.PLAYER) return
                for (unit in getUnitGroup(UnitTargetType.ANY)) {
                    checkAlert(unit)
                }
                phase = if (activeUnits.size == 0) {
                    Phase.STOPPING
                } else {
                    Phase.CHECKING
                }
            }
            Phase.CHECKING -> {
                var hasTurn = false
                loopAgents { id ->
                    if (mUnit[id].hasTurn) {
                        hasTurn = true
                        true
                    } else false
                }
                if (hasTurn) {
                    phase = Phase.PLANNING
                    currentJob = KtxAsync.launch {
                        while (!animateSystem.ready || unitManageSystem.isBusy(performingUnit)) skipFrame()
                        phase = if (plan()) Phase.PERFORMING else Phase.STOPPING
                    }
                } else {
                    phase = Phase.STOPPING
                }
            }
            Phase.PLANNING -> {
            }
            Phase.PERFORMING -> {
                assert(currentUnit >= 0)
                val unit = currentUnit
                performingUnit = unit
                mBehavior[unit].execution?.execute()
                mBehavior[unit].execution = null
                phase = Phase.CHECKING
            }
            Phase.STOPPING -> {
                loopAgents { id ->
                    unitManageSystem.endTurn(id)
                    false
                }
                loopAgents(true) { id ->
                    unitManageSystem.endTurn(id)
                    false
                }
                phase = Phase.STOPPED
            }
            Phase.STOPPED -> {
            }
        }
    }

    private var highestScore = 0f

    @Synchronized
    private fun setBestUnit(unit: Int, score: Float) {
        if (currentUnit < 0 || highestScore < score) {
            currentUnit = unit
            highestScore = score
        }
    }

    private suspend fun plan(): Boolean {
        // init
        loopAgents { id ->
            mBehavior[id].tree.reset()
            false
        }

        highestScore = 0f
        currentUnit = -1

        planJob = GlobalScope.launch {
            supervisorScope {
                loopAgents { id ->
                    launch {
                        val agent = mBehavior[id]
                        val success = agent.tree.run()
                        if (success) {
                            setBestUnit(id, agent.score)
                        }
                    }
                    false
                }
            }
        }

        planJob!!.join()
        planJob = null

        return currentUnit >= 0
    }

    @Synchronized
    fun getUnitGroup(targetType: UnitTargetType): List<Int> {
        var group = unitGroup[targetType]
        if (group == null) {
            group = when (targetType) {
                UnitTargetType.ANY -> unitManageSystem.getUnits()
                UnitTargetType.FRIENDLY -> unitManageSystem.getAllies(combatTurnSystem.faction)
                UnitTargetType.HOSTILE -> unitManageSystem.getEnemies(combatTurnSystem.faction)
            }
            unitGroup[targetType] = group
        }
        return group
    }

    fun getSortedUnitList(self: Int, targetType: UnitTargetType, condition: UnitCondition): ArrayList<Int> {
        val list = ArrayList(getUnitGroup(targetType))
        when (condition) {
            UnitCondition.WEAK -> {
                list.sortBy { mUnit[it].hp }
            }
            UnitCondition.STRONG -> {
                list.sortByDescending { mUnit[it].hp }
            }
            UnitCondition.CLOSE -> {
                list.sortBy {
                    tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile)
                }
            }
            UnitCondition.FAR -> {
                list.sortByDescending {
                    tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile)
                }
            }
        }
        return list
    }

    @Synchronized
    fun getUnit(self: Int, targetType: UnitTargetType, condition: UnitCondition): Int {
        val group = getUnitGroup(targetType)
        return when (condition) {
            UnitCondition.WEAK -> group.minBy { mUnit[it].hp }
            UnitCondition.STRONG -> group.maxBy { mUnit[it].hp }
            UnitCondition.CLOSE -> group.minBy { tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile) }
            UnitCondition.FAR -> group.maxBy { tileSystem.getDistance(mUnit[it].tile, mUnit[self].tile) }
        } ?: -1
    }

    fun getScore(result: String, apCost: Int, amount: Float): Float {
        var score = scoreMap[result] ?: 0f
        score += (amount - apCost) / 100f
        return score
    }

    fun checkAlert(unit: Int, tile: Int = -1) {
        if (idleUnits.size == 0) return
        if (mUnit[unit] == null) return
        val t = if (tile >= 0) tile else mUnit[unit].tile
        val it = idleUnits.iterator()
        while (it.hasNext()) {
            val u = it.next()
            if (!unitManageSystem.isEnemy(u, unit)) continue
            val maxDistance = 6f * 1f + (mAttributes[u].awareness - 10) * 0.05f
            if (tileSystem.getDistance(mUnit[u].tile, t) <= maxDistance) {
                it.remove()
                activeUnits.add(u)
            }
        }
    }
}
